package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.UserRequest;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.dto.RegisterRequest;
import id.co.bcaf.goceng.securities.RolePermissionEvaluator;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RolePermissionEvaluator rolePermissionEvaluator;


    // ✅ Get current authenticated user's info
    @GetMapping("/whoami")
    public ResponseEntity<User> whoAmI(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.getUserByEmail(principal.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ✅ Test Access
    @GetMapping("/test-access")
    public ResponseEntity<String> testAccess(Principal principal) {
        return ResponseEntity.ok("You are: " + principal.getName());
    }

    // ✅ Public Registration (CUSTOMER only)
    @PostMapping(
            path = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )


    // ✅ Get all users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get users by status
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

    // ✅ Admin create user
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<User> createUser(
            @RequestBody @Validated User user,
            @RequestParam UUID branchId
    ) {
        log.info("Creating user: {}", user.getEmail());
        return ResponseEntity.ok(userService.createUser(user, branchId));
    }

    // ✅ Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================== DELETE / RESTORE USER ================================================

    // Soft delete user (only SUPERADMIN can do this)
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('DELETE_USER')")
    @PostMapping("/{id}/delete")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        // Your logic here
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    // Restore user (only SUPERADMIN can do this)
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('RESTORE_USER')")
    @PostMapping("/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        // Your logic here
        if (userService.restoreUser(id)) {
            return ResponseEntity.ok("User restored to ACTIVE");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("User is not in DELETED status or does not exist.");
    }


//    public ResponseEntity<?> registerUser(@RequestBody @Validated RegisterRequest request) {
//        log.info("Registering new customer: {}", request.getEmail());
//
//        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
//            return ResponseEntity.badRequest().body("Missing required fields.");
//        }
//
//        try {
//            User registeredUser = userService.registerUser(
//                    new User(request.getName(), request.getEmail(), request.getPassword()),
//                    request.getBranchId()
//            );
//            return ResponseEntity.ok(registeredUser);
//        } catch (Exception ex) {
//            log.error("Error during registration: {}", ex.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed.");
//        }
//    }
}
