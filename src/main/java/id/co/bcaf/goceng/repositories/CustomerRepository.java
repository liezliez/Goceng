package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // ✅ Corrected method
    Optional<Customer> findByUserIdUser(UUID idUser);
}
