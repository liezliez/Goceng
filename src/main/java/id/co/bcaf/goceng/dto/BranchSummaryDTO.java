package id.co.bcaf.goceng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BranchSummaryDTO {
    private long totalUsers;
    private long totalApplications;
}
