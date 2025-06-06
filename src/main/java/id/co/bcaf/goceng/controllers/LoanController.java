package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.*;
import id.co.bcaf.goceng.models.*;
import id.co.bcaf.goceng.repositories.*;
import id.co.bcaf.goceng.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing Loan entities and related operations.
 *
 * Provides secured endpoints to:
 * - Create a loan from an approved application ({@link #createLoan})
 * - Partially update a loan ({@link #updateLoanPartially})
 * - Retrieve the total loan amount for a customer ({@link #getTotalLoan})
 * - Retrieve current authenticated user's loans ({@link #getCurrentUserLoans})
 * - Retrieve loan application history for a customer ({@link #getLoanApplicationHistory})
 * - Retrieve loan logs by loan ID ({@link #getLoanLogs})
 * - Search loans by customer, status, and date range ({@link #searchLoans})
 * - Simulate a loan calculation ({@link #simulateLoan})
 * - Get total loan amount disbursed ({@link #getTotalLoanDisbursed})
 *
 * All endpoints except {@link #getTotalLoanDisbursed} are protected by feature-level permission checks.
 */
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final ApplicationRepository applicationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final LoanApplicationService loanApplicationService;

    /**
     * Creates a loan from an approved application.
     * Requires 'CREATE_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('CREATE_LOANS')")
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

    /**
     * Partially updates a loan by its ID.
     * Requires 'MANAGE_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_LOANS')")
    @PatchMapping("/{loanId}")
    public ResponseEntity<LoanResponse> updateLoanPartially(
            @PathVariable UUID loanId,
            @RequestBody LoanUpdateRequest request
    ) {
        LoanResponse updatedLoan = loanService.updateLoanPartially(loanId, request);
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Retrieves the total loan amount for a customer.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
    @GetMapping("/customer/{customerId}/total")
    public ResponseEntity<BigDecimal> getTotalLoan(@PathVariable UUID customerId) {
        BigDecimal totalLoan = loanService.getTotalLoanForCustomer(customerId);
        return ResponseEntity.ok(totalLoan);
    }

    /**
     * Retrieves the loans for the currently authenticated user.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
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

    /**
     * Retrieves loan application history for a customer.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationHistory(@PathVariable UUID customerId) {
        List<LoanApplicationDTO> history = loanApplicationService.getLoanApplication(customerId);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves loan logs by loan ID.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
    @GetMapping("/{loanId}/logs")
    public ResponseEntity<List<LoanLog>> getLoanLogs(@PathVariable UUID loanId) {
        List<LoanLog> logs = loanService.getLoanLogs(loanId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Searches loans by optional parameters: customerId, status, date range.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
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

    /**
     * Simulates a loan based on input parameters.
     * Requires 'VIEW_LOANS' permission.
     */
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_LOANS')")
    @PostMapping("/simulate")
    public ResponseEntity<LoanResponse> simulateLoan(@RequestBody LoanSimulationRequest request) {
        BigDecimal loanAmount = BigDecimal.valueOf(request.getLoanAmount());
        BigDecimal interestRate = BigDecimal.valueOf(request.getInterestRate());
        int tenor = request.getTenor();

        CustomerResponse customer = customerService.getCustomerById(request.getCustomerId());

        LoanResponse response = loanService.simulateLoan(loanAmount, interestRate, tenor, customer);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the total loan amount disbursed.
     * No specific permission required.
     */
    @GetMapping("/total-disbursed")
    public ResponseEntity<BigDecimal> getTotalLoanDisbursed() {
        BigDecimal total = loanRepository.sumLoanAmount();
        return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
    }

}
