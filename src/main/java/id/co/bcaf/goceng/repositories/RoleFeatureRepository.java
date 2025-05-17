package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleFeatureRepository extends JpaRepository<RoleFeature, Long> {

    Optional<RoleFeature> findByRoleRoleNameAndFeatureFeatureName(String roleName, String featureName);

    List<RoleFeature> findByRole(Role role);

    @Query("SELECT rf.feature FROM RoleFeature rf WHERE rf.role.roleName = :roleName")
    List<Feature> findFeaturesByRoleRoleName(@Param("roleName") String roleName);

    Optional<RoleFeature> findByRoleAndFeature(Role role, Feature feature);

    List<RoleFeature> findByRole_IdRole(Long roleId);

}
