package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
/**
 * REST controller for managing Role entities.
 *
 * Provides secured endpoints to:
 * - List all roles ({@link #getAllRoles})
 * - Retrieve a role by ID ({@link #getRoleById})
 * - Create a new role ({@link #createRole})
 * - Delete a role by ID ({@link #deleteRole})
 *
 * All endpoints require the 'MANAGE_ROLES' feature permission,
 * enforced via the class-level {@code @PreAuthorize} annotation.
 */
@RestController
@RequestMapping("/roles")
@PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_ROLES')")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves all roles.
     * @return List of all Role entities.
     */
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Retrieves a role by its ID.
     * @param id Role ID.
     * @return Role entity if found, otherwise 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Integer id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new role.
     * @param role Role entity to create.
     * @return The saved Role entity.
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role savedRole = roleService.saveRole(role);
        return ResponseEntity.ok(savedRole);
    }

    /**
     * Deletes a role by its ID.
     * @param id Role ID to delete.
     * @return HTTP 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
