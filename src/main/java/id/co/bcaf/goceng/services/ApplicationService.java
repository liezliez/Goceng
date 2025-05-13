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

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final BranchRepository branchRepo;
    private final ApplicationLogRepository applicationLogRepo;
    private final LoanService loanService;
    private final PlafonRepository plafonRepo;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UserNotAuthenticatedException("User is not authenticated");
        }
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new UserNotAuthenticatedException("User not found: " + auth.getName()));
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        Customer customer = getCustomerById(req.getCustomerId());
        checkForPendingApplications(customer);

        Branch branch = getBranchById(req.getBranchId());
        Plafon plafon = getPlafonLimit();

        validateLoanAmount(req.getAmount(), plafon.getPlafonAmount());

        Application app = buildApplication(req, customer, branch, plafon);
        return convertToResponse(applicationRepo.save(app));
    }

    private Customer getCustomerById(UUID customerId) {
        return customerRepo.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    private void checkForPendingApplications(Customer customer) {
        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));
        if (hasPending) {
            throw new ApplicationAlreadyActiveException("Customer already has an active application");
        }
    }

    private Branch getBranchById(UUID branchId) {
        return branchRepo.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
    }

    private Plafon getPlafonLimit() {
        return plafonRepo.findFirstByOrderByPlafonAmountAsc()
                .orElseThrow(() -> new RuntimeException("No loan limit available"));
    }

    private void validateLoanAmount(BigDecimal requestedAmount, BigDecimal plafonAmount) {
        if (requestedAmount.compareTo(plafonAmount) > 0) {
            throw new LoanAmountExceededException("Requested loan amount exceeds the plafon limit.");
        }
    }

    private Application buildApplication(ApplicationRequest req, Customer customer, Branch branch, Plafon plafon) {
        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setTenor(req.getTenor());
        app.setBranch(branch);
        app.setStatus(ApplicationStatus.PENDING_MARKETING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        app.setPlafon(plafon);
        app.setInterestRate(plafon.getInterestRate());
        app.setPlafonType(plafon.getPlafonType());
        app.setPlafonLimit(plafon.getPlafonAmount());

        return app;
    }

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepo.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getApplicationById(UUID id) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        return convertToResponse(app);
    }

    @Transactional
    public ApplicationResponse approveApplication(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                                  ApplicationStatus nextStatus, ApprovalRole role) {
        Application app = getApplicationById(id).toApplication();

        validateCurrentStatus(app, currentStatus);

        User approver = getCurrentUser();
        validateRolePermission(role, approver);
        validateBranch(approver, app);

        setApprovalFields(app, approver, role);
        app.setStatus(isApproved ? nextStatus : getRejectedStatus(role));
        app.setUpdatedAt(LocalDateTime.now());

        if (isApproved && app.getStatus() == ApplicationStatus.APPROVED) {
            processLoanCreation(app);
        }

        logApplicationChange(app, approver, isApproved ? "APPROVE" : "REJECT", isApproved);

        return convertToResponse(applicationRepo.save(app));
    }

    private void processLoanCreation(Application app) {
        try {
            Plafon limit = getPlafonLimit();
            loanService.createLoanFromApprovedApplication(app, app.getCustomer(), limit.getInterestRate(), app.getTenor());
            logger.info("Loan created for application ID: {}", app.getId());
        } catch (Exception e) {
            logger.error("Failed to create loan for application ID {}: {}", app.getId(), e.getMessage(), e);
            throw new RuntimeException("Loan creation failed: " + e.getMessage());
        }
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
        String required = role.name();
        if (!approver.getRole().getRoleName().equals("ROLE_" + required))
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
                app.setNipMarketing(getNipFromApprover(approver));
            }
            case BRANCH_MANAGER -> {
                app.setBranchManagerAssigned(approver);
                app.setBranchManagerAssignedTime(now);
                app.setNipBranchManager(getNipFromApprover(approver));
            }
            case BACK_OFFICE -> {
                app.setBackOfficeAssigned(approver);
                app.setBackOfficeAssignedTime(now);
                app.setNipBackOffice(getNipFromApprover(approver));
            }
        }
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        return ApplicationStatus.valueOf("REJECTED_" + role.name());
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
                .tenor(app.getTenor())
                .limitType(app.getPlafonType())
                .limitAmount(app.getPlafonLimit())
                .interestRate(app.getInterestRate())
                .noteMarketing(app.getMarketingNote())
                .noteBranchManager(app.getBranchManagerNote())
                .noteBackOffice(app.getBackOfficeNote())
                .nipMarketing(app.getNipMarketing())
                .nipBranchManager(app.getNipBranchManager())
                .nipBackOffice(app.getNipBackOffice())
                .build();
    }

    private String getName(User user) {
        return user != null ? user.getName() : null;
    }

    private String getNipFromApprover(User approver) {
        Employee employee = employeeRepo.findById(approver.getEmployee().getId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found for user: " + approver.getUsername()));
        return employee.getNIP();
    }

    public ApplicationResponse marketingApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved, ApplicationStatus.PENDING_MARKETING, ApplicationStatus.PENDING_BRANCH_MANAGER, ApprovalRole.MARKETING);
    }

    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved, ApplicationStatus.PENDING_BRANCH_MANAGER, ApplicationStatus.PENDING_BACK_OFFICE, ApprovalRole.BRANCH_MANAGER);
    }

    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved) {
        return approveApplication(id, isApproved, ApplicationStatus.PENDING_BACK_OFFICE, ApplicationStatus.APPROVED, ApprovalRole.BACK_OFFICE);
    }

    public enum ApprovalRole {
        MARKETING, BRANCH_MANAGER, BACK_OFFICE
    }
}
