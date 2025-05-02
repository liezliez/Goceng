package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static id.co.bcaf.goceng.securities.JwtFilter.logger;

@Service
public class RoleFeatureService {

    @Autowired
    private RoleFeatureRepository roleFeatureRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FeatureRepository featureRepository;

    /**
     * Adds a feature to a specific role if not already associated.
     *
     * @param roleName    name of the role
     * @param featureName name of the feature
     * @return true if added successfully, false if already associated
     */
    @Transactional
    public boolean addFeatureToRole(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        if (roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent()) {
            logger.info("Feature '{}' is already associated with role '{}'", featureName, roleName);
            return false;
        }

        RoleFeature roleFeature = new RoleFeature();
        roleFeature.setRole(role);
        roleFeature.setFeature(feature);
        roleFeatureRepository.save(roleFeature);

        logger.info("Feature '{}' added to role '{}'", featureName, roleName);
        return true;
    }

    /**
     * Removes a feature from a role if associated.
     *
     * @param roleName    name of the role
     * @param featureName name of the feature
     * @return true if removed, false if not found
     */
    @Transactional
    public boolean removeFeatureFromRole(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        Optional<RoleFeature> roleFeatureOptional = roleFeatureRepository.findByRoleAndFeature(role, feature);
        if (roleFeatureOptional.isPresent()) {
            roleFeatureRepository.delete(roleFeatureOptional.get());
            logger.info("Feature '{}' removed from role '{}'", featureName, roleName);
            return true;
        }

        logger.warn("Feature '{}' not associated with role '{}'", featureName, roleName);
        return false;
    }

    /**
     * Checks if the role has a specific feature.
     *
     * @param roleName    role to check
     * @param featureName feature to check
     * @return true if associated
     */
    public boolean hasFeature(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);
        return roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent();
    }

    /**
     * Retrieves feature names associated with a role.
     *
     * @param roleName the role name
     * @return list of feature names
     */
    public List<String> getFeaturesByRole(final String roleName) {
        Role role = findRole(roleName);
        List<RoleFeature> roleFeatures = roleFeatureRepository.findByRole(role);
        return roleFeatures.stream()
                .map(rf -> rf.getFeature().getFeatureName())
                .collect(Collectors.toList());
    }

    /**
     * Normalizes a role name to ensure it starts with "ROLE_".
     *
     * @param roleName input role name
     * @return normalized role name
     */
    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        while (roleName.startsWith("ROLE_ROLE_")) {
            roleName = roleName.substring(5); // remove one "ROLE_"
        }

        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        return roleName;
    }

    /**
     * Fetches a Role entity by name.
     *
     * @param roleName raw or normalized role name
     * @return Role object
     */
    private Role findRole(String roleName) {
        String normalized = normalizeRoleName(roleName);
        logger.info("Looking up role: {}", normalized);
        return roleRepository.findByRoleName(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalized));
    }

    /**
     * Fetches a Feature entity by name.
     *
     * @param featureName the feature name
     * @return Feature object
     */
    private Feature findFeature(String featureName) {
        if (featureName == null || featureName.isBlank()) {
            throw new IllegalArgumentException("Feature name cannot be null or empty");
        }

        return featureRepository.findByFeatureName(featureName)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + featureName));
    }
}
