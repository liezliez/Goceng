package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.dto.ApprovalRequest;
import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PutMapping("/{id}/approve/marketing")
    public ResponseEntity<ApplicationResponse> marketingApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest
    ) {
        System.out.println("Received ApprovalRequest JSON: approved = " + approvalRequest.isApproved() + ", note = " + approvalRequest.getNote());
        return ResponseEntity.ok(applicationService.marketingApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
    }


    @PutMapping("/{id}/approve/branch-manager")
    public ResponseEntity<ApplicationResponse> branchManagerApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest
    ) {
        return ResponseEntity.ok(applicationService.branchManagerApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
    }

    @PutMapping("/{id}/approve/back-office")
    public ResponseEntity<ApplicationResponse> backOfficeApprove(
            @PathVariable UUID id,
            @RequestBody ApprovalRequest approvalRequest
    ) {
        return ResponseEntity.ok(applicationService.backOfficeApprove(id, approvalRequest.isApproved(), approvalRequest.getNote()));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @RequestParam ApplicationService.ApprovalRole role,
            @RequestParam(required = false) String note  // Add note here, optional
    ) {
        return ResponseEntity.ok(applicationService.rejectApplication(id, role, note));
    }

}
