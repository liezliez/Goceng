package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleFeatureService {

    @Autowired
    private RoleFeatureRepository roleFeatureRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FeatureRepository featureRepository;

    /**
     * Adds a feature to a specific role.
     *
     * @param roleName    The name of the role (e.g., "SUPERADMIN").
     * @param featureName The name of the feature (e.g., "DELETE_USER").
     * @return true if the feature was successfully added, false otherwise.
     */
    public boolean addFeatureToRole(String roleName, String featureName) {
        roleName = normalizeRoleName(roleName);

        // Fetch the Role by roleName
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            return false; // Role not found
        }

        // Fetch the Feature by featureName
        Optional<Feature> featureOptional = featureRepository.findByFeatureName(featureName);
        if (featureOptional.isEmpty()) {
            return false; // Feature not found
        }

        // Create a new RoleFeature association and save it to the database
        RoleFeature roleFeature = new RoleFeature();
        roleFeature.setRole(roleOptional.get());
        roleFeature.setFeature(featureOptional.get());
        roleFeatureRepository.save(roleFeature);
        return true;
    }

    /**
     * Removes a feature from a specific role.
     *
     * @param roleName    The name of the role (e.g., "SUPERADMIN").
     * @param featureName The name of the feature (e.g., "DELETE_USER").
     * @return true if the feature was successfully removed, false otherwise.
     */
    public boolean removeFeatureFromRole(String roleName, String featureName) {
        roleName = normalizeRoleName(roleName);

        // Fetch the Role by roleName
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            return false; // Role not found
        }

        // Fetch the Feature by featureName
        Optional<Feature> featureOptional = featureRepository.findByFeatureName(featureName);
        if (featureOptional.isEmpty()) {
            return false; // Feature not found
        }

        Role role = roleOptional.get();
        Feature feature = featureOptional.get();

        // Delete the RoleFeature association from the database
        RoleFeature roleFeature = roleFeatureRepository.findByRoleAndFeature(role, feature);
        if (roleFeature != null) {
            roleFeatureRepository.delete(roleFeature);
            return true;
        }
        return false; // RoleFeature association not found
    }

    /**
     * Checks if a role has a specific feature.
     *
     * @param roleName    The name of the role.
     * @param featureName The name of the feature.
     * @return true if the role has the feature, false otherwise.
     */
    public boolean hasFeature(String roleName, String featureName) {
        roleName = normalizeRoleName(roleName);

        // Check if the RoleFeature association exists
        RoleFeature roleFeature = roleFeatureRepository.findByRoleRoleNameAndFeatureFeatureName(roleName, featureName);
        return roleFeature != null;
    }

    /**
     * Normalize the role name to ensure it starts with "ROLE_".
     *
     * @param roleName The role name to normalize.
     * @return The normalized role name.
     */
    private String normalizeRoleName(String roleName) {
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return roleName;
    }
}
