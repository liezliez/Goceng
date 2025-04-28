package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.services.RoleFeatureService;
import id.co.bcaf.goceng.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {

    private final RoleFeatureService roleFeatureService;
    private final RoleRepository roleRepository;

    @GetMapping("/{roleName}")
    public List<RoleFeature> getRoleFeatures(@PathVariable String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return roleFeatureService.getFeaturesByRole(role);
    }
}
