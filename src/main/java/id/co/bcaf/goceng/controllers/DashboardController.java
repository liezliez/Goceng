package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.BranchSummaryDTO;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for dashboard-related data retrieval.
 *
 * Provides an endpoint to retrieve summary statistics for a given branch,
 * including the number of users in the branch, total loan applications for
 * the branch, and total users overall.
 *
 * The data is returned as a {@link BranchSummaryDTO}.
 */

@RestController
@RequestMapping("/dashboard")
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
