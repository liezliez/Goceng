package id.co.bcaf.goceng.repositories;


import id.co.bcaf.goceng.models.LoanLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanLimitRepository extends JpaRepository<LoanLimit, UUID> {
}
