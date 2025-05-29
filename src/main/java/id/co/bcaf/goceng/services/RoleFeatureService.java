package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleFeatureService {

    private static final Logger logger = LoggerFactory.getLogger(RoleFeatureService.class);

    private final RoleFeatureRepository roleFeatureRepository;
    private final RoleRepository roleRepository;
    private final FeatureRepository featureRepository;

    @Transactional
    public boolean addFeatureToRole(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        boolean exists = roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent();
        if (exists) {
            logger.debug("Feature '{}' already associated with role '{}'", featureName, roleName);
            return false;
        }

        RoleFeature roleFeature = new RoleFeature();
        roleFeature.setRole(role);
        roleFeature.setFeature(feature);
        roleFeatureRepository.save(roleFeature);

        logger.info("Added feature '{}' to role '{}'", featureName, roleName);
        return true;
    }

    @Transactional
    public boolean removeFeatureFromRole(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);

        return roleFeatureRepository.findByRoleAndFeature(role, feature)
                .map(roleFeature -> {
                    roleFeatureRepository.delete(roleFeature);
                    logger.info("Removed feature '{}' from role '{}'", featureName, roleName);
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("Feature '{}' not associated with role '{}'", featureName, roleName);
                    return false;
                });
    }

    public boolean hasFeature(final String roleName, final String featureName) {
        Role role = findRole(roleName);
        Feature feature = findFeature(featureName);
        boolean has = roleFeatureRepository.findByRoleAndFeature(role, feature).isPresent();
        logger.debug("Role '{}' has feature '{}': {}", roleName, featureName, has);
        return has;
    }

    public List<String> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(feature -> feature.getFeatureName())
                .collect(Collectors.toList());
    }


    public List<String> getFeaturesByRole(final String roleName) {
        Role role = findRole(roleName);
        List<RoleFeature> roleFeatures = roleFeatureRepository.findByRole(role);
        List<String> features = roleFeatures.stream()
                .map(rf -> rf.getFeature().getFeatureName())
                .collect(Collectors.toList());
        logger.debug("Features for role '{}': {}", roleName, features);
        return features;
    }

    public List<String> getFeaturesByRoleId(Long roleId) {
        Role role = roleRepository.findById(Math.toIntExact(roleId))
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        return roleFeatureRepository.findByRole(role).stream()
                .map(rf -> rf.getFeature().getFeatureName())
                .collect(Collectors.toList());
    }

    private Role findRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        // Do NOT normalize, expect role names as-is from authorities (e.g., ROLE_ADMIN)
        logger.debug("Looking up role by name: {}", roleName);
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }

    private Feature findFeature(String featureName) {
        if (featureName == null || featureName.isBlank()) {
            throw new IllegalArgumentException("Feature name cannot be null or empty");
        }

        return featureRepository.findByFeatureName(featureName)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + featureName));
    }
}
