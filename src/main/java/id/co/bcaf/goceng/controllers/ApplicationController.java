package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.dto.ApprovalRequest;
import id.co.bcaf.goceng.enums.ApprovalRole;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing loan applications.
 *
 * Provides endpoints to:
 * - {@link #create(ApplicationRequest)}: Create new loan applications.
 * - {@link #approveApplication(UUID, String, ApprovalRequest)}: Approve applications by role (marketing, branch manager, back office).
 * - {@link #rejectApplication(UUID, ApprovalRole, String)}: Reject applications specifying role and optional note.
 * - {@link #autoApproveApplication(UUID, String)}: Auto-approve applications with optional note.
 * - {@link #getAllApplications()}: Retrieve all applications.
 * - {@link #getApplicationsByCurrentUserBranch()}: Get applications by current user's branch.
 * - {@link #getApplicationById(UUID)}: Get application by its ID.
 * - {@link #getApplicationsByCustomer(UUID)}: Get applications by customer ID.
 * - {@link #getApplicationsByCustomerOrUserId(UUID)}: Get applications by customer or user ID.
 *
 * Security:
 * Uses role-feature-based access control with {@code @PreAuthorize} annotations,
 * delegating permission checks to {@code RolePermissionEvaluator}.
 */


@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('CREATE_APPLICATION')")
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PutMapping("/{id}/approve/{role}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('APPROVE_APPLICATION')")
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
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('REJECT_APPLICATION')")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @RequestParam ApprovalRole role,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(applicationService.rejectApplication(id, role, note));
    }

    @PutMapping("/{id}/auto-approve")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('AUTO_APPROVE_APPLICATION')")
    public ResponseEntity<ApplicationResponse> autoApproveApplication(
            @PathVariable UUID id,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(applicationService.autoApproveApplication(id, note));
    }

    @GetMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_APPLICATION')")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/branch")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_APPLICATION')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCurrentUserBranch() {
        List<Application> applications = applicationService.getApplicationsByCurrentUserBranch();
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_APPLICATION')")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/by-customer/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_APPLICATION')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCustomer(@PathVariable UUID id) {
        List<Application> applications = applicationService.getApplicationsByCustomer(id);
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/by-customer-or-user/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_APPLICATION')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCustomerOrUserId(@PathVariable UUID id) {
        List<Application> applications = applicationService.getApplicationsByCustomerOrUserId(id);
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::fromEntity).collect(Collectors.toList()));
    }
}

