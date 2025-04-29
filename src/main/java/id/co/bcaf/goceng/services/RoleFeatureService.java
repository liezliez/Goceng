package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        // Avoid creating duplicate RoleFeature associations
        if (roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent()) {
            return false; // Feature already associated with role
        }

        // Create and save new RoleFeature association
        RoleFeature roleFeature = new RoleFeature();
        roleFeature.setRole(role);
        roleFeature.setFeature(feature);
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
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        Optional<RoleFeature> roleFeatureOptional = Optional.ofNullable(roleFeatureRepository.findByRoleAndFeature(role, feature));
        if (roleFeatureOptional.isPresent()) {
            roleFeatureRepository.delete(roleFeatureOptional.get());
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
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        return roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent();
    }

    /**
     * Fetch all features associated with a specific role.
     *
     * @param roleName The name of the role.
     * @return A list of feature names associated with the role.
     */
    public List<String> getFeaturesByRole(String roleName) {
        Role role = findRole(roleName);

        List<RoleFeature> roleFeatures = roleFeatureRepository.findByRole(role);

        return roleFeatures.stream()
                .map(roleFeature -> roleFeature.getFeature().getFeatureName())
                .collect(Collectors.toList());
    }

    /**
     * Normalize the role name to ensure it starts with "ROLE_".
     *
     * @param roleName The role name to normalize.
     * @return The normalized role name.
     */
    private String normalizeRoleName(String roleName) {
        if (roleName.startsWith("ROLE_ROLE_")) {
            roleName = roleName.substring(5);  // Remove the extra 'ROLE_' prefix
        } else if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return roleName;
    }

    /**
     * Fetch a role by its name.
     *
     * @param roleName The name of the role to find.
     * @return The role if found.
     * @throws IllegalArgumentException if the role is not found.
     */
    private Role findRole(String roleName) {
        return roleRepository.findByRoleName(normalizeRoleName(roleName))
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }

    /**
     * Fetch a feature by its name.
     *
     * @param featureName The name of the feature to find.
     * @return The feature if found.
     * @throws IllegalArgumentException if the feature is not found.
     */
    private Feature findFeature(String featureName) {
        return featureRepository.findByFeatureName(featureName)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + featureName));
    }
}
