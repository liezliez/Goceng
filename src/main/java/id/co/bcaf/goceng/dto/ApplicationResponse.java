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

    // Updated field names to reflect new naming conventions
    private String marketingAssignedName;
    private String branchManagerAssignedName;
    private String backOfficeAssignedName;

    private LocalDateTime marketingAssignedTime;
    private LocalDateTime branchManagerAssignedTime;
    private LocalDateTime backOfficeAssignedTime;
}
