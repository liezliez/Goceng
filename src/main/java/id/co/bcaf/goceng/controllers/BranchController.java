package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.BranchRequest;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.services.BranchService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Branch entities.
 *
 * Provides secured endpoints to:
 * - Create a new branch ({@link #createBranch})
 * - Update an existing branch ({@link #updateBranch})
 * - Retrieve a branch by ID ({@link #getBranchById})
 * - Delete a branch ({@link #deleteBranch})
 * - List all branches ({@link #getAllBranches})
 *
 * All methods are protected by a feature-level permission check:
 * {@code @rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_BRANCHES')}.
 */


@RestController
@RequestMapping("/branches")
@PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_BRANCHES')")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public Branch createBranch(@RequestBody @Valid BranchRequest request) {
        return branchService.createBranch(request);
    }

    @PutMapping("/{id}")
    public Branch updateBranch(@PathVariable UUID id, @RequestBody @Valid BranchRequest request) {
        return branchService.updateBranch(id, request);
    }

    @GetMapping("/{id}")
    public Branch getBranchById(@PathVariable UUID id) {
        return branchService.getBranchById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
    }

    @GetMapping
    public List<Branch> getAllBranches() {
        return branchService.getAllBranches();
    }
}
