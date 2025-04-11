package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    // You can add custom query methods here if needed
}
