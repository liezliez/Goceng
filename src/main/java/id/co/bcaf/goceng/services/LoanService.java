package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.exceptions.ResourceNotFoundException;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.models.LoanLog;
import id.co.bcaf.goceng.repositories.LoanLogRepository;
import id.co.bcaf.goceng.repositories.LoanRepository;
import id.co.bcaf.goceng.dto.LoanResponse;
import id.co.bcaf.goceng.dto.LoanUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanLogRepository loanLogRepository;

    public Loan createLoanFromApprovedApplication(Application application, Customer customer, BigDecimal interestRate, int tenor) {
        BigDecimal amount = application.getAmount();
        BigDecimal monthlyInstallment = calculateInstallment(amount, interestRate, tenor);

        Loan loan = Loan.builder()
                .customer(customer)
                .application(application)
                .loanAmount(amount)
                .tenor(tenor)
                .remainingTenor(tenor)
                .remainingPrincipal(amount)
                .interestRate(interestRate)
                .installment(monthlyInstallment)
                .totalPaid(BigDecimal.ZERO)
                .disbursedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Loan.LoanStatus.ACTIVE)
                .build();


        return loanRepository.save(loan);
    }

    private BigDecimal calculateInstallment(BigDecimal principal, BigDecimal annualInterestRate, int tenorMonths) {
        // Calculate monthly interest rate as decimal
        BigDecimal monthlyInterest = annualInterestRate.divide(BigDecimal.valueOf(12), 10, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP);

        if (monthlyInterest.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenorMonths), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterest);
        // Use pow with positive exponent, calculate denominator as 1 - (1+r)^-tenor
        BigDecimal denominator = BigDecimal.ONE.subtract(onePlusR.pow(-tenorMonths, java.math.MathContext.DECIMAL128));
        BigDecimal numerator = principal.multiply(monthlyInterest);

        return numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    public LoanResponse simulateLoan(BigDecimal loanAmount, BigDecimal interestRate, int tenor, CustomerResponse customer) {
        BigDecimal monthlyInstallment = calculateInstallment(loanAmount, interestRate, tenor);

        return LoanResponse.builder()
                .id(null) // Not persisted yet
                .customerId(customer.getIdCustomer())
                .customerName(customer.getName())
                .loanAmount(loanAmount)
                .tenor(tenor)
                .installment(monthlyInstallment)
                .interest(interestRate)
                .remainingTenor(tenor)
                .remainingPrincipal(loanAmount)
                .totalPaid(BigDecimal.ZERO)
                .plafonType("Standard") // Example value
                .plafonLimit(BigDecimal.valueOf(1_000_000)) // Example plafon limit
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
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

    private LoanResponse convertToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName())
                .loanAmount(loan.getLoanAmount())
                .tenor(loan.getTenor())
                .installment(loan.getInstallment())
                .interest(loan.getInterestRate())
                .remainingTenor(loan.getRemainingTenor())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .totalPaid(loan.getTotalPaid())
                .plafonType("Standard") // You may want to add dynamic logic here
                .plafonLimit(BigDecimal.valueOf(1_000_000)) // Example limit, can be dynamic
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    public BigDecimal getTotalLoanForCustomer(UUID customerId) {
        return loanRepository.sumTotalLoanAmountByCustomer(customerId).orElse(BigDecimal.ZERO);
    }

//    public List<LoanResponse> getLoanHistoryForCustomer(UUID customerId) {
//        return loanRepository.findByCustomerId(customerId).stream()
//                .map(this::convertToResponse)
//                .toList();
//    }



    public LoanResponse getLoanById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return convertToResponse(loan);
    }


    public List<LoanLog> getLoanLogs(UUID loanId) {
        return loanLogRepository.findByLoan_IdOrderByTimestampDesc(loanId);
    }

    // Optional: Use this to track field changes if you want
    private void logChange(Loan loan, String field, String oldVal, String newVal, String username) {
        LoanLog log = LoanLog.builder()
                .loan(loan)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .action("UPDATE")
                .timestamp(LocalDateTime.now())
                .performedBy(username)
                .build();

        loanLogRepository.save(log);
    }

    public List<LoanResponse> searchLoans(UUID customerId, Loan.LoanStatus status, LocalDateTime fromDate, LocalDateTime toDate) {
        List<Loan> loans = loanRepository.searchLoansWithFilters(customerId, status, fromDate, toDate);
        return loans.stream()
                .map(this::convertToResponse)
                .toList();
    }
}
