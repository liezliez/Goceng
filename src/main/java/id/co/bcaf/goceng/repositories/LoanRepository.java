package id.co.bcaf.goceng.repositories;


import id.co.bcaf.goceng.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    @Query("SELECT SUM(l.loanAmount) FROM Loan l WHERE l.customer.id = :customerId")
    Optional<BigDecimal> sumLoanByCustomer(@Param("customerId") UUID customerId);

}
