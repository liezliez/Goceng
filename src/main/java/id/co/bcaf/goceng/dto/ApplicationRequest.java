package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ApplicationRequest {
    private UUID customerId;
    private BigDecimal amount;
    private String purpose;
    private Integer tenor;

    private String noteMarketing;

//
//    private UUID branchId;
//    private UUID loanLimitId;
}
