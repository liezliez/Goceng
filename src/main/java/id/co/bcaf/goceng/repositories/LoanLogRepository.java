package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.LoanLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanLogRepository extends JpaRepository<LoanLog, UUID> {
    List<LoanLog> findByLoanIdOrderByTimestampDesc(UUID loanId);
}

