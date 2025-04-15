package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.ApplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, UUID> {
}
