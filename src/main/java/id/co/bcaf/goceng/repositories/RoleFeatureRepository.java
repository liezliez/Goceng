package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleFeatureRepository extends JpaRepository<RoleFeature, Integer> {
    List<RoleFeature> findAllByRole(Role role);
}
