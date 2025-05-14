package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.dto.ApprovalRequest;  // Import the new ApprovalRequest DTO
import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ✅ Create new application
    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    // ✅ Progress application approval for Marketing
    @PutMapping("/{id}/approve/marketing")
    public ResponseEntity<ApplicationResponse> marketingApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest  // Accept the approval data in the request body
    ) {
        return ResponseEntity.ok(applicationService.marketingApprove(id, approvalRequest.isApproved()));
    }

    // ✅ Progress application approval for Branch Manager
    @PutMapping("/{id}/approve/branch-manager")
    public ResponseEntity<ApplicationResponse> branchManagerApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest  // Accept the approval data in the request body
    ) {
        return ResponseEntity.ok(applicationService.branchManagerApprove(id, approvalRequest.isApproved()));
    }

    // ✅ Progress application approval for Back Office
    @PutMapping("/{id}/approve/back-office")
    public ResponseEntity<ApplicationResponse> backOfficeApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest  // Accept the approval data in the request body
    ) {
        return ResponseEntity.ok(applicationService.backOfficeApprove(id, approvalRequest.isApproved()));
    }

    // ✅ Get all applications
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    // ✅ Get application by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    // ✅ Reject application at any stage
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @RequestParam ApplicationService.ApprovalRole role
    ) {
        return ResponseEntity.ok(applicationService.rejectApplication(id, role));
    }
}
