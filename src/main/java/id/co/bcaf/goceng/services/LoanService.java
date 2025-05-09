package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.exceptions.ResourceNotFoundException;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.repositories.LoanRepository;
import id.co.bcaf.goceng.dto.LoanResponse;
import id.co.bcaf.goceng.dto.LoanUpdateRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    public Loan createLoanFromApprovedApplication(Application application, Customer customer, BigDecimal interestRate, int tenor) {
        BigDecimal amount = application.getAmount(); // ✅ BigDecimal for amount
        BigDecimal monthlyInstallment = calculateInstallment(amount, interestRate, tenor);

        Loan loan = Loan.builder()
                .customer(customer)
                .loanAmount(amount)
                .tenor(tenor)
                .remainingTenor(tenor)
                .remainingPrincipal(amount)
                .interestRate(interestRate)
                .installment(monthlyInstallment)
                .totalPaid(BigDecimal.ZERO) // Start with zero total paid
                .disbursedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now()) // Set createdAt
                .updatedAt(LocalDateTime.now()) // Set updatedAt
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        return loanRepository.save(loan);
    }

    private BigDecimal calculateInstallment(BigDecimal principal, BigDecimal annualInterestRate, int tenorMonths) {
        BigDecimal monthlyInterest = annualInterestRate.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP).divide(BigDecimal.valueOf(100));
        BigDecimal numerator = principal.multiply(monthlyInterest);
        BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.add(monthlyInterest).pow(-tenorMonths));
        return numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    @Transactional
    public LoanResponse updateLoanPartially(UUID loanId, LoanUpdateRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (request.getTenor() != null) loan.setTenor(request.getTenor());
        if (request.getInstallment() != null) loan.setInstallment(request.getInstallment());
        if (request.getInterest() != null) loan.setInterestRate(request.getInterest());
        if (request.getRemainingPrincipal() != null) loan.setRemainingPrincipal(request.getRemainingPrincipal());

        loan.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(loanRepository.save(loan));
    }

    // Helper method to convert Loan to LoanResponse
    private LoanResponse convertToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getIdCustomer())
                .customerName(loan.getCustomer().getName()) // Access the name from Customer
                .loanAmount(loan.getLoanAmount())
                .tenor(loan.getTenor())
                .installment(loan.getInstallment())
                .interest(loan.getInterestRate())
                .remainingTenor(loan.getRemainingTenor())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .totalPaid(loan.getTotalPaid())
                .plafonType("Standard") // You can update this as needed
                .plafonLimit(BigDecimal.valueOf(1000000)) // Replace with actual plafon limit
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
