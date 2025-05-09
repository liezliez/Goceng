package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.exceptions.*;
import id.co.bcaf.goceng.models.*;
import id.co.bcaf.goceng.repositories.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final BranchRepository branchRepo;
    private final ApplicationLogRepository applicationLogRepo;
    private final LoanService loanService;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new UserNotAuthenticatedException("User is not authenticated");
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new UserNotAuthenticatedException("User not found: " + auth.getName()));
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));

        if (hasPending) throw new ApplicationAlreadyActiveException("Customer already has an active application");

        Branch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setBranch(branch);
        app.setStatus(ApplicationStatus.PENDING_MARKETING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(applicationRepo.save(app));
    }

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepo.findAll().stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public ApplicationResponse getApplicationById(UUID id) {
        return convertToResponse(applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found")));
    }

    @Transactional
    public ApplicationResponse approveApplication(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                                  ApplicationStatus nextStatus, ApprovalRole role) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        validateCurrentStatus(app, currentStatus);
        User approver = getCurrentUser();
        validateRolePermission(role, approver);
        validateBranch(approver, app);

        setApprovalFields(app, approver, role);
        app.setStatus(isApproved ? nextStatus : getRejectedStatus(role));
        app.setUpdatedAt(LocalDateTime.now());

        if (isApproved && app.getStatus() == ApplicationStatus.APPROVED) {
            Loan loan = loanService.createLoanFromApprovedApplication(
                    app, app.getCustomer(), BigDecimal.valueOf(12), 12
            );
            logger.info("Loan created for application ID: {}", app.getId());
        }

        logApplicationChange(app, approver, isApproved ? "APPROVE" : "REJECT", isApproved);
        return convertToResponse(applicationRepo.save(app));
    }

    @Transactional
    public ApplicationResponse rejectApplication(UUID id, ApprovalRole role) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        User approver = getCurrentUser();
        validateRolePermission(role, approver);
        validateBranch(approver, app);
        app.setStatus(getRejectedStatus(role));
        app.setUpdatedAt(LocalDateTime.now());

        logApplicationChange(app, approver, "REJECT", false);
        return convertToResponse(applicationRepo.save(app));
    }

    private void validateCurrentStatus(Application app, ApplicationStatus expectedStatus) {
        if (app.getStatus() != expectedStatus)
            throw new InvalidApplicationStatusException("Expected status: " + expectedStatus);
    }

    private void validateRolePermission(ApprovalRole role, User approver) {
        String required = switch (role) {
            case MARKETING -> "ROLE_MARKETING";
            case BRANCH_MANAGER -> "ROLE_BRANCH_MANAGER";
            case BACK_OFFICE -> "ROLE_BACK_OFFICE";
        };
        if (!approver.getRole().getRoleName().equals(required))
            throw new InsufficientPermissionsException("Required role: " + required);
    }

    private void validateBranch(User approver, Application app) {
        Employee emp = employeeRepo.findByUser_IdUser(approver.getIdUser())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        if (!emp.getBranch().equals(app.getBranch()))
            throw new UnauthorizedBranchException("Unauthorized to process application from another branch");
    }

    private void setApprovalFields(Application app, User approver, ApprovalRole role) {
        LocalDateTime now = LocalDateTime.now();
        switch (role) {
            case MARKETING -> {
                app.setMarketingAssigned(approver);
                app.setMarketingAssignedTime(now);
            }
            case BRANCH_MANAGER -> {
                app.setBranchManagerAssigned(approver);
                app.setBranchManagerAssignedTime(now);
            }
            case BACK_OFFICE -> {
                app.setBackOfficeAssigned(approver);
                app.setBackOfficeAssignedTime(now);
            }
        }
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        return switch (role) {
            case MARKETING -> ApplicationStatus.REJECTED_MARKETING;
            case BRANCH_MANAGER -> ApplicationStatus.REJECTED_BRANCH_MANAGER;
            case BACK_OFFICE -> ApplicationStatus.REJECTED_BACK_OFFICE;
        };
    }

    private void logApplicationChange(Application app, User approver, String action, boolean isApproved) {
        applicationLogRepo.save(ApplicationLog.builder()
                .applicationId(app.getId())
                .action(action)
                .changedBy(approver.getUsername())
                .timestamp(LocalDateTime.now())
                .beforeStatus(app.getStatus().name())
                .afterStatus(app.getStatus().name())
                .details(isApproved ? "Application approved" : "Application rejected")
                .build());
    }

    private ApplicationResponse convertToResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .customerId(app.getCustomer().getId())
                .customerName(app.getCustomer().getUser().getName())
                .amount(app.getAmount())
                .purpose(app.getPurpose())
                .status(app.getStatus().name())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .marketingAssignedName(getName(app.getMarketingAssigned()))
                .branchManagerAssignedName(getName(app.getBranchManagerAssigned()))
                .backOfficeAssignedName(getName(app.getBackOfficeAssigned()))
                .marketingAssignedTime(app.getMarketingAssignedTime())
                .branchManagerAssignedTime(app.getBranchManagerAssignedTime())
                .backOfficeAssignedTime(app.getBackOfficeAssignedTime())
                .build();
    }

    private String getName(User user) {
        return user != null ? user.getName() : null;
    }

    public ApplicationResponse marketingApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApprovalRole.MARKETING);
    }

    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApprovalRole.BRANCH_MANAGER);
    }

    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApplicationStatus.APPROVED,
                ApprovalRole.BACK_OFFICE);
    }

    public enum ApprovalRole {
        MARKETING, BRANCH_MANAGER, BACK_OFFICE
    }
}
