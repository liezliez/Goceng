package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.*;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.models.LoanLog;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.services.CustomerService;
import id.co.bcaf.goceng.services.LoanApplicationService;
import id.co.bcaf.goceng.services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final ApplicationRepository applicationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final LoanApplicationService loanApplicationService;

    // Create loan from an approved application
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

        LoanResponse loanResponse = loanService.getLoanById(loan.getId());
        return ResponseEntity.ok(loanResponse);
    }

    // Partial update of loan
    @PatchMapping("/{loanId}")
    public ResponseEntity<LoanResponse> updateLoanPartially(
            @PathVariable UUID loanId,
            @RequestBody LoanUpdateRequest request
    ) {
        LoanResponse updatedLoan = loanService.updateLoanPartially(loanId, request);
        return ResponseEntity.ok(updatedLoan);
    }

    // Get total loan amount for customer
    @GetMapping("/customer/{customerId}/total")
    public ResponseEntity<BigDecimal> getTotalLoan(@PathVariable UUID customerId) {
        BigDecimal totalLoan = loanService.getTotalLoanForCustomer(customerId);
        return ResponseEntity.ok(totalLoan);
    }

    @GetMapping("/current")
    public ResponseEntity<List<LoanResponse>> getCurrentUserLoans() {
        Optional<Customer> currentCustomerOpt = customerService.getCustomerFromCurrentUser();

        if (currentCustomerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        Customer currentCustomer = currentCustomerOpt.get();
        List<LoanResponse> loans = loanService.getLoansByCustomerId(currentCustomer.getId());
        return ResponseEntity.ok(loans);
    }


    // Get loan application history for customer
    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationHistory(@PathVariable UUID customerId) {
        List<LoanApplicationDTO> history = loanApplicationService.getLoanApplication(customerId);
        return ResponseEntity.ok(history);
    }

    // Get loan logs
    @GetMapping("/{loanId}/logs")
    public ResponseEntity<List<LoanLog>> getLoanLogs(@PathVariable UUID loanId) {
        List<LoanLog> logs = loanService.getLoanLogs(loanId);
        return ResponseEntity.ok(logs);
    }

    // Search loans with optional filters
    @GetMapping("/search")
    public ResponseEntity<List<LoanResponse>> searchLoans(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) Loan.LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<LoanResponse> loans = loanService.searchLoans(customerId, status, from, to);
        return ResponseEntity.ok(loans);
    }

    // Simulate loan calculation
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
