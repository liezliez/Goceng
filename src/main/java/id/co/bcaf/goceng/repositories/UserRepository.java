package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by account status
    List<User> findByAccountStatus(AccountStatus accountStatus);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by ID (idUser)
    Optional<User> findByIdUser(UUID idUser);

    // Find user by role
    List<User> findByRole(Role role);
}
