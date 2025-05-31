package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.BranchSummaryDTO;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_DASHBOARD')")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping("/branch-summary")
    public BranchSummaryDTO getBranchSummary(@RequestParam UUID branchId) {
        long branchUsers = userRepository.countByBranchId(branchId);
        long totalApplications = applicationRepository.countByBranchId(branchId);
        long totalUsersOverall = userRepository.count();
        return new BranchSummaryDTO(branchUsers, totalApplications, totalUsersOverall);
    }
}
