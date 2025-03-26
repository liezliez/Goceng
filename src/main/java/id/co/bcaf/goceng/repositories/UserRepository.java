package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByAccountStatus(AccountStatus accountStatus);
    Optional<User> findByEmail(String email); // Find user by email
}
