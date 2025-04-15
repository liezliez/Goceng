package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
}
