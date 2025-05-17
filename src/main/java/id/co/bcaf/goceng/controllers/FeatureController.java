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

    @GetMapping
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(featureService.getAllFeatures());
    }

    @PostMapping
    public ResponseEntity<Feature> createFeature(@RequestBody Feature feature) {
        return ResponseEntity.ok(featureService.createFeature(feature));
    }

    @GetMapping("/my-features")
    public ResponseEntity<List<Feature>> getMyFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            System.out.println("UserDetails is null - unauthenticated access");
            return ResponseEntity.status(401).build();
        }

        System.out.println("Authenticated user: " + userDetails.getUsername());
        System.out.println("Authorities: " + userDetails.getAuthorities());

        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No role found for current user"))
                .getAuthority();

//        while (roleName.startsWith("ROLE_")) {
//            roleName = roleName.substring(5);
//        }

        List<Feature> features = featureService.getFeaturesByRoleName(roleName);

        return ResponseEntity.ok(features);
    }

}
