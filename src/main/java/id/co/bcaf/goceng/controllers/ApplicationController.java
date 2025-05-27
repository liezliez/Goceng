package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.dto.ApprovalRequest;
import id.co.bcaf.goceng.enums.ApprovalRole;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PutMapping("/{id}/approve/{role}")
    public ResponseEntity<ApplicationResponse> approveApplication(
            @PathVariable UUID id,
            @PathVariable String role,
            @RequestBody ApprovalRequest approvalRequest
    ) {
        switch (role.toLowerCase()) {
            case "marketing":
                return ResponseEntity.ok(applicationService.marketingApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
            case "branch-manager":
                return ResponseEntity.ok(applicationService.branchManagerApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
            case "back-office":
                return ResponseEntity.ok(applicationService.backOfficeApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
            default:
                throw new IllegalArgumentException("Invalid approval role: " + role);
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @RequestParam ApprovalRole role,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(applicationService.rejectApplication(id, role, note));
    }

    @PutMapping("/{id}/auto-approve")
    public ResponseEntity<ApplicationResponse> autoApproveApplication(
            @PathVariable UUID id,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(applicationService.autoApproveApplication(id, note));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/branch")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCurrentUserBranch() {
        List<Application> applications = applicationService.getApplicationsByCurrentUserBranch();
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/by-customer/{id}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCustomer(@PathVariable UUID id) {
        List<Application> applications = applicationService.getApplicationsByCustomer(id);
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/by-customer-or-user/{id}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCustomerOrUserId(@PathVariable UUID id) {
        List<Application> applications = applicationService.getApplicationsByCustomerOrUserId(id);
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }
}
