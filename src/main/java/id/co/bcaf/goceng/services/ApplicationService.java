package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.ApplicationLog;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.BranchRepository;
import id.co.bcaf.goceng.repositories.ApplicationLogRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final BranchRepository branchRepo;
    private final ApplicationLogRepository applicationLogRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new IllegalStateException("User is not authenticated");
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        Customer customer = customerRepo.findByIdCustomer(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));

        if (hasPending) {
            throw new RuntimeException("Customer already has an active application");
        }

        Branch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

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

    @Transactional
    public ApplicationResponse marketingApprove(UUID id, boolean isApproved) {
        return handleApproval(id, isApproved, ApplicationStatus.PENDING_MARKETING, ApplicationStatus.PENDING_BRANCH_MANAGER, ApprovalRole.MARKETING);
    }

    @Transactional
    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved) {
        return handleApproval(id, isApproved, ApplicationStatus.PENDING_BRANCH_MANAGER, ApplicationStatus.PENDING_BACK_OFFICE, ApprovalRole.BRANCH_MANAGER);
    }

    @Transactional
    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved) {
        return handleApproval(id, isApproved, ApplicationStatus.PENDING_BACK_OFFICE, ApplicationStatus.APPROVED, ApprovalRole.BACK_OFFICE);
    }

    private enum ApprovalRole {
        MARKETING, BRANCH_MANAGER, BACK_OFFICE
    }

    @Transactional
    public ApplicationResponse handleApproval(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                              ApplicationStatus nextStatus, ApprovalRole role) {

        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != currentStatus) {
            throw new IllegalStateException("Application is not in the correct approval stage: " + currentStatus);
        }

        User approver = getCurrentUser();

        // Role validation
        String requiredRole = switch (role) {
            case MARKETING -> "ROLE_MARKETING";
            case BRANCH_MANAGER -> "ROLE_BRANCH_MANAGER";
            case BACK_OFFICE -> "ROLE_BACK_OFFICE";
        };

        boolean hasRequiredRole = approver.getRole().getRole_name().equals(requiredRole);

        if (!hasRequiredRole) {
            throw new SecurityException("You do not have the required role to approve this application. Required: " + requiredRole);
        }

        Employee employee = employeeRepo.findByUser_IdUser(approver.getIdUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getBranch().equals(app.getBranch())) {
            throw new IllegalStateException("You are not authorized to approve this application from a different branch.");
        }

        setApprovalFields(app, approver, role);

        String beforeStatus = app.getStatus().name();

        if (isApproved) {
            app.setStatus(nextStatus);
        } else {
            app.setStatus(getRejectedStatus(role));
        }

        app.setUpdatedAt(LocalDateTime.now());

        String afterStatus = app.getStatus().name();

        // Determine the action for the log (APPROVE or REJECT)
        String action = isApproved ? "APPROVE" : "REJECT";

        // Log the application change (approval/rejection)
        logApplicationChange(app.getId(), action, approver.getUsername(), beforeStatus, afterStatus,
                isApproved ? "Application approved" : "Application rejected");

        // Save the application (this should update the status in the database)
        return convertToResponse(applicationRepo.save(app));
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        return switch (role) {
            case MARKETING -> ApplicationStatus.REJECTED_MARKETING;
            case BRANCH_MANAGER -> ApplicationStatus.REJECTED_BRANCH_MANAGER;
            case BACK_OFFICE -> ApplicationStatus.REJECTED_BACK_OFFICE;
        };
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

    private ApplicationResponse convertToResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .customerId(app.getCustomer().getIdCustomer())
                .customerName(app.getCustomer().getUser().getName())
                .amount(app.getAmount())
                .purpose(app.getPurpose())
                .status(app.getStatus().name())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .marketingAssignedName(app.getMarketingAssigned() != null ? app.getMarketingAssigned().getName() : null)
                .branchManagerAssignedName(app.getBranchManagerAssigned() != null ? app.getBranchManagerAssigned().getName() : null)
                .backOfficeAssignedName(app.getBackOfficeAssigned() != null ? app.getBackOfficeAssigned().getName() : null)
                .marketingAssignedTime(app.getMarketingAssignedTime())
                .branchManagerAssignedTime(app.getBranchManagerAssignedTime())
                .backOfficeAssignedTime(app.getBackOfficeAssignedTime())
                .build();
    }

    private void logApplicationChange(UUID applicationId, String action, String changedBy, String beforeStatus, String afterStatus, String details) {
        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setAction(action);
        log.setChangedBy(changedBy);
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setDetails(details);

        applicationLogRepository.save(log);
    }

}
