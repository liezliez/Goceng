package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.services.BranchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public List<Branch> getAllBranches() {
        return branchService.getAllBranches();
    }
}
