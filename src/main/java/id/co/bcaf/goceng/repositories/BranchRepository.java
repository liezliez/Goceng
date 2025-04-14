package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    // Custom queries can be added here if necessary, but for now, the basic findById should work
}
