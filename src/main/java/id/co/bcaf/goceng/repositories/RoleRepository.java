package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
