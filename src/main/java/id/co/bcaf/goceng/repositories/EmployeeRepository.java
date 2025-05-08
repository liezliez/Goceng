package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {


    // ✅ Optimistic Locking for updates
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT e FROM Employee e WHERE e.id_employee = :id_employee")
    Optional<Employee> findByIdWithLock(@Param("id_employee") UUID id_employee);

    boolean existsByUser(User user);

    // ✅ Correct: Fetch employee by the user's idUser field
    Optional<Employee> findByUser_IdUser(UUID idUser);

    Optional<Employee> findTopByNIPStartingWithOrderByNIPDesc(String prefix);

}
