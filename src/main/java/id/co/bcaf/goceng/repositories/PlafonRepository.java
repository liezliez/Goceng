package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Plafon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlafonRepository extends JpaRepository<Plafon, UUID> {
    Optional<Plafon> findFirstByOrderByPlafonAmountAsc();
}
