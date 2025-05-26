package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.VerificationToken;
import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
}
