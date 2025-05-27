package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.dto.LoanResponse;
import id.co.bcaf.goceng.dto.LoanUpdateRequest;
import id.co.bcaf.goceng.exceptions.ResourceNotFoundException;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.models.LoanLog;
import id.co.bcaf.goceng.repositories.LoanLogRepository;
import id.co.bcaf.goceng.repositories.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        BigDecimal monthlyInterest = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        if (monthlyInterest.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenorMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterest);
        BigDecimal onePlusRPowerN = onePlusR.pow(tenorMonths, MathContext.DECIMAL128);
        BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(onePlusRPowerN, 10, RoundingMode.HALF_UP));
        BigDecimal numerator = principal.multiply(monthlyInterest);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }


//    public LoanResponse simulateLoan(BigDecimal loanAmount, BigDecimal interestRate, int tenor, CustomerResponse customer) {
//        BigDecimal monthlyInstallment = calculateInstallment(loanAmount, interestRate, tenor);
//
//        return LoanResponse.builder()
//                .id(null)
//                .customerId(customer.getIdCustomer())
//                .customerName(customer.getName())
//                .loanAmount(loanAmount)
//                .tenor(tenor)
//                .installment(monthlyInstallment)
//                .interest(interestRate)
//                .remainingTenor(tenor)
//                .remainingPrincipal(loanAmount)
//                .totalPaid(BigDecimal.ZERO)
//                .plafonType("Standard")
//                .plafonLimit(BigDecimal.valueOf(1_000_000))
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }

    public LoanResponse simulateLoan(BigDecimal loanAmount, BigDecimal interestRate, int tenor, CustomerResponse customer) {
        BigDecimal monthlyInstallment = calculateInstallment(loanAmount, interestRate, tenor);

        return LoanResponse.builder()
                .id(null)
                .customerId(customer.getIdCustomer())
                .customerName(customer.getName())
                .loanAmount(loanAmount)
                .tenor(tenor)
                .installment(monthlyInstallment)
                .interest(interestRate)
                .remainingTenor(tenor)
                .remainingPrincipal(loanAmount)
                .totalPaid(BigDecimal.ZERO)
                .plafonType(customer.getPlafonType()) // use from response
                .plafonLimit(customer.getCreditLimit()) // use from response
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
        var customer = loan.getCustomer();
        var plafon = customer.getPlafon();

        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .loanAmount(loan.getLoanAmount())
                .tenor(loan.getTenor())
                .installment(loan.getInstallment())
                .interest(loan.getInterestRate())
                .remainingTenor(loan.getRemainingTenor())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .totalPaid(loan.getTotalPaid())
                .disbursedAt(loan.getDisbursedAt())
                .plafonType(plafon != null ? plafon.getPlafonType() : null)
                .plafonLimit(plafon != null ? plafon.getPlafonAmount() : null)
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    public BigDecimal getTotalLoanForCustomer(UUID customerId) {
        return loanRepository.sumTotalLoanAmountByCustomer(customerId).orElse(BigDecimal.ZERO);
    }

    public List<LoanResponse> getLoansByCustomerId(UUID customerId) {
        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        return loans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    public LoanResponse getLoanById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return convertToResponse(loan);
    }

    public List<LoanLog> getLoanLogs(UUID loanId) {
        return loanLogRepository.findByLoan_IdOrderByTimestampDesc(loanId);
    }

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
                .collect(Collectors.toList());
    }
}
