package id.co.bcaf.goceng.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
public class LoanApplicationDTO {

    // From Application
    private UUID applicationId;
    private LocalDateTime applicationCreatedAt;
    private BigDecimal applicationAmount;
    private String applicationStatus;
    private Integer applicationTenor;
    private String purpose;

    // From Loan (optional, may be null if no loan yet)
    private UUID loanId;
    private BigDecimal loanAmount;
    private Integer loanTenor;
    private BigDecimal loanInstallment;
    private BigDecimal loanInterestRate;
    private String loanStatus;
    private LocalDateTime loanDisbursedAt;

}
