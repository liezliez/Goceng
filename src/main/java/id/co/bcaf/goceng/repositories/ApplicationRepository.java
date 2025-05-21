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

    boolean existsByCustomerAndStatusIn(Customer customer, List<ApplicationStatus> statusList);


    List<Application> findByCustomer(Customer customer);

    List<Application> findByBranch_Id(UUID branchId);

    List<Application> findByCustomer_Id(UUID customerId); // FIXED

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByCustomer_User_idUser(UUID idUser);

}
