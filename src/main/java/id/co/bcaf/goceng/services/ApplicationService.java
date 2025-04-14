package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo; // Add repository for Employee

    // Create Application
    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        // Check if the customer exists
        Customer customer = customerRepo.findByIdCustomer(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if the customer already has an ongoing application
        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));

        if (hasPending) {
            throw new RuntimeException("Customer already has an active application");
        }

        // Create new application and set the branch from the request
        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setBranch(req.getBranch());  // Set the branch from the request
        app.setStatus(ApplicationStatus.PENDING_MARKETING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        // Save the application
        applicationRepo.save(app);

        return convertToResponse(app);
    }

    // Progress Application Status for Marketing Approval
    @Transactional
    public ApplicationResponse marketingApprove(UUID id, boolean isApproved, User approver) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Check if the user is the correct marketing approver based on branch
        Employee employee = employeeRepo.findById(approver.getIdUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getBranch().equals(app.getBranch())) {
            // Reject application if branch doesn't match
            app.setStatus(ApplicationStatus.REJECTED);
            app.setMarketingApprover(approver);
            app.setMarketingApprovalTime(LocalDateTime.now());
            app.setUpdatedAt(LocalDateTime.now());
            applicationRepo.save(app);
            return convertToResponse(app);
        }

        if (app.getStatus() != ApplicationStatus.PENDING_MARKETING) {
            throw new IllegalStateException("Application is not in Marketing approval stage");
        }

        app.setMarketingApprover(approver);
        app.setMarketingApprovalTime(LocalDateTime.now());
        app.setStatus(isApproved ? ApplicationStatus.PENDING_BRANCH_MANAGER : ApplicationStatus.REJECTED);
        app.setUpdatedAt(LocalDateTime.now());

        applicationRepo.save(app);
        return convertToResponse(app);
    }

    // Progress Application Status for Branch Manager Approval
    @Transactional
    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved, User approver) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Check if the user is the correct branch manager approver based on branch
        Employee employee = employeeRepo.findById(approver.getIdUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getBranch().equals(app.getBranch())) {
            // Reject application if branch doesn't match
            app.setStatus(ApplicationStatus.REJECTED);
            app.setBranchManagerApprover(approver);
            app.setBranchManagerApprovalTime(LocalDateTime.now());
            app.setUpdatedAt(LocalDateTime.now());
            applicationRepo.save(app);
            return convertToResponse(app);
        }

        if (app.getStatus() != ApplicationStatus.PENDING_BRANCH_MANAGER) {
            throw new IllegalStateException("Application is not in Branch Manager approval stage");
        }

        app.setBranchManagerApprover(approver);
        app.setBranchManagerApprovalTime(LocalDateTime.now());
        app.setStatus(isApproved ? ApplicationStatus.PENDING_BACK_OFFICE : ApplicationStatus.REJECTED);
        app.setUpdatedAt(LocalDateTime.now());

        applicationRepo.save(app);
        return convertToResponse(app);
    }

    // Progress Application Status for Back Office Approval
    @Transactional
    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved, User approver) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Check if the user is the correct back office approver based on branch
        Employee employee = employeeRepo.findById(approver.getIdUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getBranch().equals(app.getBranch())) {
            // Reject application if branch doesn't match
            app.setStatus(ApplicationStatus.REJECTED);
            app.setBackOfficeApprover(approver);
            app.setBackOfficeApprovalTime(LocalDateTime.now());
            app.setUpdatedAt(LocalDateTime.now());
            applicationRepo.save(app);
            return convertToResponse(app);
        }

        if (app.getStatus() != ApplicationStatus.PENDING_BACK_OFFICE) {
            throw new IllegalStateException("Application is not in Back Office approval stage");
        }

        app.setBackOfficeApprover(approver);
        app.setBackOfficeApprovalTime(LocalDateTime.now());
        app.setStatus(isApproved ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        app.setUpdatedAt(LocalDateTime.now());

        applicationRepo.save(app);
        return convertToResponse(app);
    }

    // Helper: Convert Entity to Response DTO
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
