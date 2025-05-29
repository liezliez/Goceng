package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.RoleFeatureService;
import id.co.bcaf.goceng.securities.UserDetailsServiceImpl;
import id.co.bcaf.goceng.utils.JwtUtil;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {

    private final RoleFeatureService roleFeatureService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @PostMapping("/add-feature")
    public ResponseEntity<Map<String, String>> addFeatureToRole(@RequestParam String roleName,
                                                                @RequestParam String featureName) {
        if (roleName.isEmpty() || featureName.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role name and feature name must not be empty."));
        }

        boolean added = roleFeatureService.addFeatureToRole(roleName, featureName);
        if (added) {
            return ResponseEntity.ok(Map.of("message", "Feature successfully added to role."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Feature already associated with the role or invalid inputs."));
        }
    }

    @DeleteMapping("/remove-feature")
    public ResponseEntity<Map<String, String>> removeFeatureFromRole(@RequestParam String roleName,
                                                                     @RequestParam String featureName) {
        if (roleName.isEmpty() || featureName.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Role name and feature name must not be empty."));
        }

        boolean removed = roleFeatureService.removeFeatureFromRole(roleName, featureName);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Feature successfully removed from role."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Feature not associated with the role or invalid inputs."));
        }
    }

    /**
     * Check if a role has a specific feature.
     */
    @GetMapping("/has-feature")
    public ResponseEntity<String> hasFeature(@RequestParam String roleName,
                                             @RequestParam String featureName) {
        if (roleName.isEmpty() || featureName.isEmpty()) {
            return ResponseEntity.badRequest().body("Role name and feature name must not be empty.");
        }

        boolean hasFeature = roleFeatureService.hasFeature(roleName, featureName);
        return hasFeature
                ? ResponseEntity.ok("Role has the feature.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role does not have the feature.");
    }

    /**
     * Get features available to the currently authenticated user.
     */
    @GetMapping("/features")
    public ResponseEntity<?> getFeaturesForCurrentUser() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
        }

        var authorities = userDetails.getAuthorities();
        if (authorities.isEmpty()) {
            return ResponseEntity.badRequest().body("No role associated with the user.");
        }

        String role = authorities.iterator().next().getAuthority();
        List<String> features = roleFeatureService.getFeaturesByRole(role);

        if (features.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No features found for this role.");
        }

        return ResponseEntity.ok(features);
    }

    @GetMapping("/features-by-role")
    public ResponseEntity<List<String>> getFeaturesByRole(@RequestParam String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> features = roleFeatureService.getFeaturesByRole(roleName);
        return ResponseEntity.ok(features);
    }

    @GetMapping("/all-features")
    public ResponseEntity<List<String>> getAllFeatures() {
        List<String> allFeatures = roleFeatureService.getAllFeatures();
        return ResponseEntity.ok(allFeatures);
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof String ? (String) principal : null;
    }

    private Optional<UserDetails> getUserDetails(String email) {
        try {
            return Optional.of(userDetailsServiceImpl.loadUserByUsername(email));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getRoleFromUserDetails(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "")) // Remove "ROLE_" prefix for display
                .orElse(null);
    }
}
