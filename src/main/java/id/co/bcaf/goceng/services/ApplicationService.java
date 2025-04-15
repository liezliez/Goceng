package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.BranchRepository;
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

        System.out.println("Received branchId: " + req.getBranchId());

        // Fetch the Branch entity based on the branchId
        Branch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setBranch(branch); // Set the correct Branch reference
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

    private ApplicationResponse handleApproval(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                               ApplicationStatus nextStatus, ApprovalRole role) {

        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != currentStatus) {
            throw new IllegalStateException("Application is not in the correct approval stage: " + currentStatus);
        }

        User approver = getCurrentUser();
        Employee employee = employeeRepo.findByUser_IdUser(approver.getIdUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if the approver's branch matches the application branch
        if (!employee.getBranch().equals(app.getBranch())) {
            throw new IllegalStateException("You are not authorized to approve this application from a different branch.");
        }

        setApprovalFields(app, approver, role);

        // Set status based on approval or rejection
        if (isApproved) {
            app.setStatus(nextStatus); // Move to the next stage if approved
        } else {
            app.setStatus(getRejectedStatus(role)); // Set rejection status based on the role
        }

        app.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(applicationRepo.save(app));
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        switch (role) {
            case MARKETING:
                return ApplicationStatus.REJECTED_MARKETING;
            case BRANCH_MANAGER:
                return ApplicationStatus.REJECTED_BRANCH_MANAGER;
            case BACK_OFFICE:
                return ApplicationStatus.REJECTED_BACK_OFFICE;
            default:
                throw new IllegalStateException("Unexpected value: " + role);
        }
    }

    private void setApprovalFields(Application app, User approver, ApprovalRole role) {
        LocalDateTime now = LocalDateTime.now();

        switch (role) {
            case MARKETING -> {
                app.setMarketingApprover(approver);
                app.setMarketingApprovalTime(now);
            }
            case BRANCH_MANAGER -> {
                app.setBranchManagerApprover(approver);
                app.setBranchManagerApprovalTime(now);
            }
            case BACK_OFFICE -> {
                app.setBackOfficeApprover(approver);
                app.setBackOfficeApprovalTime(now);
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
                .marketingApproverName(app.getMarketingApprover() != null ? app.getMarketingApprover().getName() : null)
                .branchManagerApproverName(app.getBranchManagerApprover() != null ? app.getBranchManagerApprover().getName() : null)
                .backOfficeApproverName(app.getBackOfficeApprover() != null ? app.getBackOfficeApprover().getName() : null)
                .marketingApprovalTime(app.getMarketingApprovalTime())
                .branchManagerApprovalTime(app.getBranchManagerApprovalTime())
                .backOfficeApprovalTime(app.getBackOfficeApprovalTime())
                .build();
    }
}
