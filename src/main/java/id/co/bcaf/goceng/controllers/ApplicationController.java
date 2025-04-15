package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam boolean isApproved
    ) {
        return ResponseEntity.ok(applicationService.marketingApprove(id, isApproved));
    }

    // ✅ Progress application approval for Branch Manager
    @PutMapping("/{id}/approve/branch-manager")
    public ResponseEntity<ApplicationResponse> branchManagerApprove(
            @PathVariable UUID id,
            @RequestParam boolean isApproved
    ) {
        return ResponseEntity.ok(applicationService.branchManagerApprove(id, isApproved));
    }

    // ✅ Progress application approval for Back Office
    @PutMapping("/{id}/approve/back-office")
    public ResponseEntity<ApplicationResponse> backOfficeApprove(
            @PathVariable UUID id,
            @RequestParam boolean isApproved
    ) {
        return ResponseEntity.ok(applicationService.backOfficeApprove(id, isApproved));
    }
}
