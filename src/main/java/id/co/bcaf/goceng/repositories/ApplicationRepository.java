package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    // You can add custom query methods here if needed
    boolean existsByCustomerAndStatusIn(Customer customer, List<ApplicationStatus> statusList);

    List<Application> findByCustomer(Customer customer);


}
