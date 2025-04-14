package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.models.User;
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

//
//    // ✅ Get all applications
//    @GetMapping
//    public ResponseEntity<List<ApplicationResponse>> getAll() {
//        return ResponseEntity.ok(applicationService.findAll());
//    }
//
//    // ✅ Get one application
//    @GetMapping("/{id}")
//    public ResponseEntity<ApplicationResponse> getById(@PathVariable UUID id) {
//        return ResponseEntity.ok(applicationService.findById(id));
//    }

//    // ✅ Update application
//    @PutMapping("/{id}")
//    public ResponseEntity<ApplicationResponse> update(
//            @PathVariable UUID id,
//            @RequestBody ApplicationRequest request
//    ) {
//        return ResponseEntity.ok(applicationService.update(id, request));
//    }

//    // ✅ Delete application
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable UUID id) {
//        applicationService.delete(id);
//        return ResponseEntity.noContent().build();
//    }

    // ✅ Progress application approval
    @PutMapping("/{id}/approve/marketing")
    public ResponseEntity<ApplicationResponse> marketingApprove(
            @PathVariable UUID id,
            @RequestParam boolean isApproved,
            @AuthenticationPrincipal User approver
    ) {
        return ResponseEntity.ok(applicationService.marketingApprove(id, isApproved, approver));
    }

    @PutMapping("/{id}/approve/branch-manager")
    public ResponseEntity<ApplicationResponse> branchManagerApprove(
            @PathVariable UUID id,
            @RequestParam boolean isApproved,
            @AuthenticationPrincipal User approver
    ) {
        return ResponseEntity.ok(applicationService.branchManagerApprove(id, isApproved, approver));
    }

    @PutMapping("/{id}/approve/back-office")
    public ResponseEntity<ApplicationResponse> backOfficeApprove(
            @PathVariable UUID id,
            @RequestParam boolean isApproved,
            @AuthenticationPrincipal User approver
    ) {
        return ResponseEntity.ok(applicationService.backOfficeApprove(id, isApproved, approver));
    }

}
