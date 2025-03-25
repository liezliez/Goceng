package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);  // ✅ Add this method

}
