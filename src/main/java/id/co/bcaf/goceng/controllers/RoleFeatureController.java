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

@RestController
@RequestMapping("/api/v1/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {

    private final RoleFeatureService roleFeatureService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    // Add a feature to a role
    @PostMapping("/add-feature")
    public ResponseEntity<String> addFeatureToRole(@RequestParam String roleName, @RequestParam String featureName) {
        boolean added = roleFeatureService.addFeatureToRole(roleName, featureName);

        if (added) {
            return new ResponseEntity<>("Feature added to role successfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to add feature to role. Check if the role and feature exist.", HttpStatus.BAD_REQUEST);
        }
    }

    // Remove a feature from a role
    @DeleteMapping("/remove-feature")
    public ResponseEntity<String> removeFeatureFromRole(@RequestParam String roleName, @RequestParam String featureName) {
        boolean removed = roleFeatureService.removeFeatureFromRole(roleName, featureName);

        if (removed) {
            return new ResponseEntity<>("Feature removed from role successfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to remove feature from role. Ensure the role and feature are correctly associated.", HttpStatus.BAD_REQUEST);
        }
    }

    // Check if a role has a specific feature
    @GetMapping("/has-feature")
    public ResponseEntity<String> hasFeature(@RequestParam String roleName, @RequestParam String featureName) {
        boolean hasFeature = roleFeatureService.hasFeature(roleName, featureName);

        if (hasFeature) {
            return new ResponseEntity<>("Role has the feature.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Role does not have the feature.", HttpStatus.NOT_FOUND);
        }
    }

    // Get features of the current logged-in user
    @GetMapping("/features")
    public ResponseEntity<?> getFeaturesForCurrentUser() {
        // Extract the current user's email from SecurityContextHolder
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Load the user details using the UserDetailsServiceImpl
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);
        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority()) // "ROLE_<role>"
                .orElse(null);

        // Retrieve the features associated with the user's role
        if (roleName == null || roleName.isEmpty()) {
            return new ResponseEntity<>("No role associated with the user.", HttpStatus.BAD_REQUEST);
        }

        // Fetch features associated with the role
        var features = roleFeatureService.getFeaturesByRole(roleName);

        // If no features are associated, return a message stating so
        if (features.isEmpty()) {
            return new ResponseEntity<>("No features found for this role.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(features, HttpStatus.OK);
    }
}
