package id.co.bcaf.goceng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BranchSummaryDTO {
    private long branchUsers;
    private long totalApplications;
    private long totalUsersOverall;
}