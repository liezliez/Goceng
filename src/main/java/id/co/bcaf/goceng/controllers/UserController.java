package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.RegisterRequest;
import id.co.bcaf.goceng.dto.UserRequest;
import id.co.bcaf.goceng.dto.UserResponse;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserByPrincipal(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.getUserByEmail(principal.getName())
                .map(user -> {
                    UserResponse dto = new UserResponse();
                    dto.setId(user.getIdUser());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setAccount_status(user.getStatus());
                    dto.setRole(user.getRole().getRoleName());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @GetMapping("/test-access")
    public ResponseEntity<String> testAccess(Principal principal) {
        return ResponseEntity.ok("You are: " + principal.getName());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Validated RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            return ResponseEntity.badRequest().body("Missing required fields.");
        }

        try {
            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(request.getPassword());

            User registeredUser = userService.registerUser(newUser, request.getBranchId());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception ex) {
            log.error("Error during registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed.");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status) {
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(userService.getUsersByStatus(accountStatus));
        } catch (IllegalArgumentException e) {
            log.error("Invalid account status: {}", status);
            return ResponseEntity.badRequest().body("Invalid account status value.");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody @Validated User user, @RequestParam UUID branchId) {
        log.info("Creating user: {}", user.getEmail());
        return ResponseEntity.ok(userService.createUser(user, branchId));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/id/{id}/edit")
//    @PreAuthorize("hasAuthority('EDIT_USER')")
    public ResponseEntity<UserResponse> editUser(@PathVariable UUID id, @RequestBody @Validated UserRequest request) {
        log.info("Editing user with ID: {}", id);
        return userService.updateUserFromRequest(id, request)
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getIdUser());
                    response.setName(user.getName());
                    response.setEmail(user.getEmail());
                    response.setAccount_status(user.getStatus());
                    response.setRole(user.getRole().getRoleName());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

//    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('DELETE_USER')")
    @PutMapping("/id/{id}/delete")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

//    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('RESTORE_USER')")
    @PutMapping("/id/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        if (userService.restoreUser(id)) {
            return ResponseEntity.ok("User restored to ACTIVE");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("User is not in DELETED status or does not exist.");
    }


    @GetMapping("/count-by-status")
    public ResponseEntity<Map<AccountStatus, Long>> countUsersByStatus() {
        return ResponseEntity.ok(userService.countUsersGroupedByStatus());
    }

    @RestController
    @RequestMapping("/roles")
    public class RoleController {

        @Autowired
        private RoleRepository roleRepository;

        @GetMapping
        public ResponseEntity<List<Role>> getAllRoles() {
            return ResponseEntity.ok(roleRepository.findAll());
        }
    }

}
