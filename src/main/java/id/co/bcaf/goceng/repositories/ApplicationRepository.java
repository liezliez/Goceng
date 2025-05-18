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

    // Checks if a customer has an application in any of the given statuses
    boolean existsByCustomerAndStatusIn(Customer customer, List<ApplicationStatus> statusList);

    // Retrieves all applications for a given customer
    List<Application> findByCustomer(Customer customer);

//    List<Application> findByBranchId(UUID branchId);

    List<Application> findByBranch_Id(UUID branchId);


    List<Application> findByCustomerId(UUID customerId);

    List<Application> findByStatus(ApplicationStatus status);
    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByCustomer_User_idUser(UUID idUser);

}
