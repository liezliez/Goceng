package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ApplicationRequest {
    private UUID customerId;
    private String purpose;
    private BigDecimal amount;
}
