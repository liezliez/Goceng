package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.RoleFeatureService;
import id.co.bcaf.goceng.securities.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {

    private final RoleFeatureService roleFeatureService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * Assign a feature to a role.
     */
    @PostMapping("/add-feature")
    public ResponseEntity<String> addFeatureToRole(@RequestParam String roleName,
                                                   @RequestParam String featureName) {
        boolean added = roleFeatureService.addFeatureToRole(roleName, featureName);

        return added
                ? ResponseEntity.ok("Feature added to role successfully!")
                : ResponseEntity.badRequest().body("Feature already associated with role or inputs are invalid.");
    }

    /**
     * Remove a feature from a role.
     */
    @DeleteMapping("/remove-feature")
    public ResponseEntity<String> removeFeatureFromRole(@RequestParam String roleName,
                                                        @RequestParam String featureName) {
        boolean removed = roleFeatureService.removeFeatureFromRole(roleName, featureName);

        return removed
                ? ResponseEntity.ok("Feature removed from role successfully!")
                : ResponseEntity.badRequest().body("Feature not associated with role or inputs are invalid.");
    }

    /**
     * Check if a role has a specific feature.
     */
    @GetMapping("/has-feature")
    public ResponseEntity<String> hasFeature(@RequestParam String roleName,
                                             @RequestParam String featureName) {
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
        // Get current user's email
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof String email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user session.");
        }

        // Load user details and get role
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);
        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority()) // Should be like "ROLE_ADMIN"
                .orElse(null);

        if (roleName == null || roleName.isEmpty()) {
            return ResponseEntity.badRequest().body("No role associated with the user.");
        }

        List<String> features = roleFeatureService.getFeaturesByRole(roleName);

        if (features.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No features found for this role.");
        }

        return ResponseEntity.ok(features);
    }
}
