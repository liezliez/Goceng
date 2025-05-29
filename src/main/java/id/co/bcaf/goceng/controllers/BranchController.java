package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.BranchRequest;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.services.BranchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/branches")
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
