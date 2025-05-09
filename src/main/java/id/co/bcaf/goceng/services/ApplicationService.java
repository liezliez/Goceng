package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.exceptions.*;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.ApplicationLog;
import id.co.bcaf.goceng.repositories.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final BranchRepository branchRepo;
    private final ApplicationLogRepository applicationLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class); // replace with your actual class name



    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName(); // This returns the email (username)
            return userRepo.findByEmail(email)
                    .orElseThrow(() -> new UserNotAuthenticatedException("User not found with email: " + email));
        }

        throw new UserNotAuthenticatedException("User is not authenticated");
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

        if (hasPending) {
            throw new ApplicationAlreadyActiveException("Customer already has an active application");
        }

        Branch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        Application app = buildNewApplication(req, customer, branch);
        return convertToResponse(applicationRepo.save(app));
    }

    private Application buildNewApplication(ApplicationRequest req, Customer customer, Branch branch) {
        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setBranch(branch);
        app.setStatus(ApplicationStatus.PENDING_MARKETING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        return app;
    }

    @Transactional
    public ApplicationResponse approveApplication(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                                  ApplicationStatus nextStatus, ApprovalRole role) {
        // Fetch the application by ID using the repository method
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found")); // Throw exception if not found

        // Validate if the application is in the expected current status
        validateCurrentStatus(app, currentStatus);

        User approver = getCurrentUser(); // Get the current user (approver)

        // Validate if the approver has permission to approve based on their role
        validateRolePermission(role, approver);

        // Ensure approver is from the same branch as the application
        validateBranch(approver, app);

        // Set the approval fields (assign the approver and the time of assignment)
        setApprovalFields(app, approver, role);

        // Update application status based on approval or rejection
        updateApplicationStatus(app, isApproved, nextStatus, role);

        // Log the approval/rejection action
        logApplicationChange(app, approver, isApproved ? "APPROVE" : "REJECT", isApproved);

        // Save the updated application and return the response
        return convertToResponse(applicationRepo.save(app)); // Return the converted response
    }



//    private Application getApplicationById(UUID id) {
//        return applicationRepo.findById(id)
//                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
//    }

    // Get all applications
    public List<ApplicationResponse> getAllApplications() {
        List<Application> applications = applicationRepo.findAll(); // Retrieve all applications
        return applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()); // Convert to response DTOs
    }

    // Get application by ID
    public ApplicationResponse getApplicationById(UUID id) {
        Application app = applicationRepo.findById(id) // Fetch the application using the repository
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found")); // Throw exception if not found
        return convertToResponse(app); // Convert the application to response DTO
    }


    // Reject application at any stage
    @Transactional
    public ApplicationResponse rejectApplication(UUID id, ApprovalRole role) {
        // Fetch the application by ID using the repository method
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found")); // Throw exception if not found

        User approver = getCurrentUser(); // Get the current user (approver)

        // Validate if the approver has permission to reject based on their role
        validateRolePermission(role, approver);

        // Ensure approver is from the same branch as the application
        validateBranch(approver, app);

        // Get the rejected status based on the role
        ApplicationStatus rejectedStatus = getRejectedStatus(role);

        // Update the application status to the rejected status
        app.setStatus(rejectedStatus);
        app.setUpdatedAt(LocalDateTime.now()); // Update the timestamp

        // Log the rejection action
        logApplicationChange(app, approver, "REJECT", false);

        // Save the updated application and return the response
        return convertToResponse(applicationRepo.save(app)); // Return the converted response
    }



    private void validateCurrentStatus(Application app, ApplicationStatus expectedStatus) {
        if (app.getStatus() != expectedStatus) {
            throw new InvalidApplicationStatusException("Application is not in the correct approval stage: " + expectedStatus);
        }
    }

    private void validateRolePermission(ApprovalRole role, User approver) {
        String requiredRole = getRequiredRole(role);
        if (!approver.getRole().getRoleName().equals(requiredRole)) {
            throw new InsufficientPermissionsException("You do not have the required role to approve this application. Required: " + requiredRole);
        }
    }

    private String getRequiredRole(ApprovalRole role) {
        switch (role) {
            case MARKETING: return "ROLE_MARKETING";
            case BRANCH_MANAGER: return "ROLE_BRANCH_MANAGER";
            case BACK_OFFICE: return "ROLE_BACK_OFFICE";
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private void validateBranch(User approver, Application app) {
        Employee employee = employeeRepo.findByUser_IdUser(approver.getIdUser())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        if (!employee.getBranch().equals(app.getBranch())) {
            throw new UnauthorizedBranchException("You are not authorized to approve this application from a different branch.");
        }
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

    private void updateApplicationStatus(Application app, boolean isApproved, ApplicationStatus nextStatus, ApprovalRole role) {
        if (isApproved) {
            app.setStatus(nextStatus);
        } else {
            app.setStatus(getRejectedStatus(role));
        }
        app.setUpdatedAt(LocalDateTime.now());
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        switch (role) {
            case MARKETING: return ApplicationStatus.REJECTED_MARKETING;
            case BRANCH_MANAGER: return ApplicationStatus.REJECTED_BRANCH_MANAGER;
            case BACK_OFFICE: return ApplicationStatus.REJECTED_BACK_OFFICE;
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private void logApplicationChange(Application app, User approver, String action, boolean isApproved) {
        String beforeStatus = app.getStatus().name();
        String afterStatus = app.getStatus().name();

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(app.getId());
        log.setAction(action);
        log.setChangedBy(approver.getUsername());
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setDetails(isApproved ? "Application approved" : "Application rejected");

        applicationLogRepository.save(log);
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
                .marketingAssignedName(app.getMarketingAssigned() != null ? app.getMarketingAssigned().getName() : null)
                .branchManagerAssignedName(app.getBranchManagerAssigned() != null ? app.getBranchManagerAssigned().getName() : null)
                .backOfficeAssignedName(app.getBackOfficeAssigned() != null ? app.getBackOfficeAssigned().getName() : null)
                .marketingAssignedTime(app.getMarketingAssignedTime())
                .branchManagerAssignedTime(app.getBranchManagerAssignedTime())
                .backOfficeAssignedTime(app.getBackOfficeAssignedTime())
                .build();
    }

    public enum ApprovalRole {
        MARKETING, BRANCH_MANAGER, BACK_OFFICE
    }

    // helper

    public ApplicationResponse marketingApprove(UUID id, boolean isApproved) {
        return approveApplication(
                id,
                isApproved,
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApprovalRole.MARKETING
        );
    }

    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved) {
        return approveApplication(
                id,
                isApproved,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApprovalRole.BRANCH_MANAGER
        );
    }

    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved) {
        return approveApplication(
                id,
                isApproved,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApplicationStatus.APPROVED,
                ApprovalRole.BACK_OFFICE
        );
    }



}
