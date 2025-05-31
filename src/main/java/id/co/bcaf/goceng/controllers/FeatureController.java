package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.services.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    @GetMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_FEATURES')")
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(featureService.getAllFeatures());
    }

    @PostMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_FEATURES')")
    public ResponseEntity<Feature> createFeature(@RequestBody Feature feature) {
        return ResponseEntity.ok(featureService.createFeature(feature));
    }

    @GetMapping("/my-features")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Feature>> getMyFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No role found for current user"))
                .getAuthority();

        List<Feature> features = featureService.getFeaturesByRoleName(roleName);

        return ResponseEntity.ok(features);
    }
}
