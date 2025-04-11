package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
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

    // Create Application
    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        // Check the customer
        Customer customer = customerRepo.findByUserIdUser(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if the Customer had already ongoing application, puyeng rek
        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));

        // If had already ongoing application
        if (hasPending) {
            throw new RuntimeException("Customer already has an active application");
        }

        // First set up for the application
        Application app = new Application();
        app.setCustomer(customer);
        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setStatus(ApplicationStatus.PENDING_MARKETING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        applicationRepo.save(app);

        return convertToResponse(app);
    }

    // Read All Applications
    public List<ApplicationResponse> findAll() {
        return applicationRepo.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Read By ID
    public ApplicationResponse findById(UUID id) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return convertToResponse(app);
    }

    // Update Application (for example, amount or purpose)
    @Transactional
    public ApplicationResponse update(UUID id, ApplicationRequest req) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setAmount(req.getAmount());
        app.setPurpose(req.getPurpose());
        app.setUpdatedAt(LocalDateTime.now());

        applicationRepo.save(app);
        return convertToResponse(app);
    }

    // Delete Application
    @Transactional
    public void delete(UUID id) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        applicationRepo.delete(app);
    }

    // Progress Application Status
    @Transactional
    public ApplicationResponse progressStatus(UUID id, boolean isApproved, User approver) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        switch (app.getStatus()) {
            case PENDING_MARKETING:
                if (isApproved) {
                    app.setStatus(ApplicationStatus.PENDING_BRANCH_MANAGER);
                    app.setMarketingApprover(approver);
                    app.setMarketingApprovalTime(LocalDateTime.now());
                } else {
                    app.setStatus(ApplicationStatus.REJECTED);
                    app.setMarketingApprover(approver);
                    app.setMarketingApprovalTime(LocalDateTime.now());
                }
                break;

            case PENDING_BRANCH_MANAGER:
                if (isApproved) {
                    app.setStatus(ApplicationStatus.PENDING_BACK_OFFICE);
                    app.setBranchManagerApprover(approver);
                    app.setBranchManagerApprovalTime(LocalDateTime.now());
                } else {
                    app.setStatus(ApplicationStatus.REJECTED);
                    app.setBranchManagerApprover(approver);
                    app.setBranchManagerApprovalTime(LocalDateTime.now());
                }
                break;

            case PENDING_BACK_OFFICE:
                if (isApproved) {
                    app.setStatus(ApplicationStatus.APPROVED);
                    app.setBackOfficeApprover(approver);
                    app.setBackOfficeApprovalTime(LocalDateTime.now());
                } else {
                    app.setStatus(ApplicationStatus.REJECTED);
                    app.setBackOfficeApprover(approver);
                    app.setBackOfficeApprovalTime(LocalDateTime.now());
                }
                break;

            default:
                throw new IllegalStateException("Application already finalized: " + app.getStatus());
        }

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
