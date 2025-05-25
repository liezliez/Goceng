package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {


    List<Loan> findByCustomerId(UUID customerId);

    List<Loan> findByApplication_Customer_Id(UUID customerId);

    List<Loan> findByCustomer_Id(UUID customerId);

    @Query("SELECT SUM(l.loanAmount) FROM Loan l WHERE l.customer.id = :customerId")
    Optional<BigDecimal> sumTotalLoanAmountByCustomer(@Param("customerId") UUID customerId);

    @Query("SELECT l FROM Loan l " +
            "WHERE (:customerId IS NULL OR l.customer.id = :customerId) " +
            "AND (:status IS NULL OR l.status = :status) " +
            "AND (:fromDate IS NULL OR l.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR l.createdAt <= :toDate)")
    List<Loan> searchLoansWithFilters(
            @Param("customerId") UUID customerId,
            @Param("status") Loan.LoanStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
