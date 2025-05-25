package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUser_IdUser(UUID idUser);
    boolean existsByNik(String nik);
    boolean existsByAccountNo(String accountNo);
    boolean existsByUser_IdUser(UUID idUser);


}
