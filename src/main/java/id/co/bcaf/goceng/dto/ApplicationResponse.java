package id.co.bcaf.goceng.dto;

import id.co.bcaf.goceng.models.Application;
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

    private String marketingAssignedName;
    private String branchManagerAssignedName;
    private String backOfficeAssignedName;

    private LocalDateTime marketingAssignedTime;
    private LocalDateTime branchManagerAssignedTime;
    private LocalDateTime backOfficeAssignedTime;

    private Integer tenor;
    private String limitType;
    private BigDecimal limitAmount;
    private BigDecimal interestRate;
    private String noteMarketing;
    private String noteBranchManager;
    private String noteBackOffice;
    private String nipMarketing;
    private String nipBranchManager;
    private String nipBackOffice;

    public static ApplicationResponse fromEntity(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .customerId(app.getCustomer().getId())
                .customerName(app.getCustomerName())
                .amount(app.getAmount())
                .purpose(app.getPurpose())
                .status(app.getStatus().name())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .marketingAssignedName(app.getMarketingAssigned() != null ? app.getMarketingAssigned().getName() : null)
                .branchManagerAssignedName(app.getBranchManagerAssigned() != null ? app.getBranchManagerAssigned().getName() : null)
                .backOfficeAssignedName(app.getBackOfficeAssigned() != null ? app.getBackOfficeAssigned().getName() : null)
                .marketingAssignedTime(app.getMarketingAssignedTime())
                .branchManagerAssignedTime(app.getBranchManagerAssignedTime())
                .backOfficeAssignedTime(app.getBackOfficeAssignedTime())
                .tenor(app.getTenor())
                .limitType(app.getPlafonType())
                .limitAmount(app.getPlafonLimit())
                .interestRate(app.getInterestRate())
                .noteMarketing(app.getMarketingNote())
                .noteBranchManager(app.getBranchManagerNote())
                .noteBackOffice(app.getBackOfficeNote())
                .nipMarketing(app.getNipMarketing())
                .nipBranchManager(app.getNipBranchManager())
                .nipBackOffice(app.getNipBackOffice())
                .build();

    }
}
