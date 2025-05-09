package id.co.bcaf.goceng.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanUpdateRequest {
    private BigDecimal loanAmount;
    private Integer tenor;
    private BigDecimal interest;
    private BigDecimal installment;
    private Integer remainingTenor;
    private BigDecimal remainingPrincipal;
    private BigDecimal totalPaid;
}
