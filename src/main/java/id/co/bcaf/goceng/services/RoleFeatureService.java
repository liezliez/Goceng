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
        // Normalize the roleName (ensure it starts with "ROLE_")
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        // Fetch the Role by roleName
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (!roleOptional.isPresent()) {
            return false; // Role not found
        }

        // Fetch the Feature by featureName
        Optional<Feature> featureOptional = featureRepository.findByFeatureName(featureName);
        if (!featureOptional.isPresent()) {
            return false; // Feature not found
        }

        Role role = roleOptional.get();
        Feature feature = featureOptional.get();

        // Create a new RoleFeature association and save it to the database
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
        // Normalize the roleName (ensure it starts with "ROLE_")
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        // Fetch the Role by roleName
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (!roleOptional.isPresent()) {
            return false; // Role not found
        }

        // Fetch the Feature by featureName
        Optional<Feature> featureOptional = featureRepository.findByFeatureName(featureName);
        if (!featureOptional.isPresent()) {
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

    public boolean hasFeature(String roleName, String featureName) {
        System.out.println("Checking feature for role: " + roleName + ", feature: " + featureName);

        // Normalize roleName and featureName as needed
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        RoleFeature roleFeature = roleFeatureRepository.findByRoleRoleNameAndFeatureFeatureName(roleName, featureName);
        return roleFeature != null;
    }
}
