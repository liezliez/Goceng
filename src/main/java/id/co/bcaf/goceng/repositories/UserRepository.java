package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Cari user berdasarkan status akun
    List<User> findByAccountStatus(AccountStatus accountStatus);

    // Cari user berdasarkan email
    Optional<User> findByEmail(String email);

    // Cari berdasarkan ID yang benar (idUser)
    Optional<User> findByIdUser(UUID idUser);
}
