package id.co.bcaf.goceng.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LoanSimulationRequest {
    private UUID customerId;
    private Double loanAmount;
    private Double interestRate; // Annual
    private int tenor;
}
