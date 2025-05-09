package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Employee> findByIdWithLock(@Param("id") UUID id);

    boolean existsByUser_IdUser(UUID idUser);

    Optional<Employee> findByUser_IdUser(UUID idUser);

    Optional<Employee> findTopByNIPStartingWithOrderByNIPDesc(String prefix);

    // Find employee by user's email
    @Query("SELECT e FROM Employee e WHERE e.user.email = :email")
    Optional<Employee> findByUserEmail(@Param("email") String email);
}
