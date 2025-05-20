package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.dto.LoanResponse;
import id.co.bcaf.goceng.dto.LoanSimulationRequest;
import id.co.bcaf.goceng.dto.LoanUpdateRequest;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.models.LoanLog;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.services.LoanService;
import id.co.bcaf.goceng.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final ApplicationRepository applicationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;


    @PostMapping("/create-from-application/{applicationId}")
    public ResponseEntity<LoanResponse> createLoan(
            @PathVariable UUID applicationId,
            @RequestParam BigDecimal interestRate,
            @RequestParam int tenor
    ) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        Customer customer = application.getCustomer();
        Loan loan = loanService.createLoanFromApprovedApplication(application, customer, interestRate, tenor);
        return ResponseEntity.ok().body(loanService.getLoanHistoryForCustomer(customer.getId()).stream()
                .filter(l -> l.getId().equals(loan.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loan creation failed")));
    }

    @PatchMapping("/{loanId}")
    public ResponseEntity<LoanResponse> updateLoanPartially(
            @PathVariable UUID loanId,
            @RequestBody LoanUpdateRequest request
    ) {
        return ResponseEntity.ok(loanService.updateLoanPartially(loanId, request));
    }

    @GetMapping("/customer/{customerId}/total")
    public ResponseEntity<BigDecimal> getTotalLoan(@PathVariable UUID customerId) {
        return ResponseEntity.ok(loanService.getTotalLoanForCustomer(customerId));
    }

    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<List<LoanResponse>> getLoanHistory(@PathVariable UUID customerId) {
        return ResponseEntity.ok(loanService.getLoanHistoryForCustomer(customerId));
    }

    @GetMapping("/{loanId}/logs")
    public ResponseEntity<List<LoanLog>> getLoanLogs(@PathVariable UUID loanId) {
        return ResponseEntity.ok(loanService.getLoanLogs(loanId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LoanResponse>> searchLoans(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) Loan.LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(loanService.searchLoans(customerId, status, from, to));
    }

    @PostMapping("/simulate")
    public ResponseEntity<LoanResponse> simulateLoan(@RequestBody LoanSimulationRequest request) {
        BigDecimal loanAmount = BigDecimal.valueOf(request.getLoanAmount());
        BigDecimal interestRate = BigDecimal.valueOf(request.getInterestRate());
        int tenor = request.getTenor();

        CustomerResponse customer = customerService.getCustomerById(request.getCustomerId());

        LoanResponse response = loanService.simulateLoan(loanAmount, interestRate, tenor, customer);
        return ResponseEntity.ok(response);
    }

}
