package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.services.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    // Method to get all features
    @GetMapping
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(featureService.getAllFeatures());
    }

    // Method to create a new feature
    @PostMapping
    public ResponseEntity<Feature> createFeature(@RequestBody Feature feature) {
        return ResponseEntity.ok(featureService.createFeature(feature));
    }

    // Method to get the features assigned to the current user's role
    @GetMapping("/my-features")
    public ResponseEntity<List<Feature>> getMyFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found"))
                .getAuthority();

        List<Feature> features = featureService.getFeaturesByRoleName(roleName);
        return ResponseEntity.ok(features);
    }
}
