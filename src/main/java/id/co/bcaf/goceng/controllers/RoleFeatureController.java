package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.services.RoleFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role-features")
public class RoleFeatureController {

    @Autowired
    private RoleFeatureService roleFeatureService;

    @GetMapping
    public ResponseEntity<List<RoleFeature>> getAllRoleFeatures() {
        return ResponseEntity.ok(roleFeatureService.getAllRoleFeatures());
    }

    @PostMapping
    public ResponseEntity<RoleFeature> createRoleFeature(@RequestBody RoleFeature roleFeature) {
        return ResponseEntity.ok(roleFeatureService.createRoleFeature(roleFeature));
    }
}
