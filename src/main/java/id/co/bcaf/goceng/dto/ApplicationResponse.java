package id.co.bcaf.goceng.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private BigDecimal amount;
    private String purpose;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String marketingApproverName;
    private String branchManagerApproverName;
    private String backOfficeApproverName;
    private LocalDateTime marketingApprovalTime;
    private LocalDateTime branchManagerApprovalTime;
    private LocalDateTime backOfficeApprovalTime;

}
