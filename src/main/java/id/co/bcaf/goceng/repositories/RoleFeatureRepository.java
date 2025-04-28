package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleFeatureRepository extends JpaRepository<RoleFeature, Long> {

    // Find a role-feature pair by role name and feature name
    RoleFeature findByRoleRoleNameAndFeatureFeatureName(String roleName, String featureName);

    // Find all features associated with a specific role
    List<RoleFeature> findByRole(Role role);

    // Find the RoleFeature pair by Role and Feature (fixing the argument for Feature)
    RoleFeature findByRoleAndFeature(Role role, Feature feature);
}
