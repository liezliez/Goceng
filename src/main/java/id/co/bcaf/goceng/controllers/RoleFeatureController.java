package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.RoleFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {

    private final RoleFeatureService roleFeatureService;

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
}
