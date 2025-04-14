package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUserIdUser(UUID idUser);  // Find customer by user ID

    // Add this new method to find by id_customer
    Optional<Customer> findByIdCustomer(UUID idCustomer);  // Find customer by id_customer
}
