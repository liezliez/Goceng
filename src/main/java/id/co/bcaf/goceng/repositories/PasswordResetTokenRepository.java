package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.PasswordResetToken;
import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
