package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByUser_IdUser(UUID idUser);

    boolean existsByUser_IdUser(UUID idUser);

    Optional<Customer> findById(UUID id);

}
