package id.co.bcaf.goceng.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;

    private BigDecimal loanAmount;
    private Integer tenor;
    private BigDecimal installment;
    private BigDecimal interest;

    private Integer remainingTenor;
    private BigDecimal remainingPrincipal;
    private BigDecimal totalPaid;

    private String plafonType;
    private BigDecimal plafonLimit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
