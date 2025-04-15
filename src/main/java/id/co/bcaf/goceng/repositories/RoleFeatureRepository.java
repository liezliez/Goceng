package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.RoleFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleFeatureRepository extends JpaRepository<RoleFeature, Integer> {

}
