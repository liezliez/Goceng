package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.UserRequest;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.dto.RegisterRequest;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ✅ Get current authenticated user's info
    @GetMapping("/whoami")
    public ResponseEntity<User> whoAmI(Principal principal) {
        log.info("Fetching current authenticated user: {}", principal.getName());
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<User> user = userService.getUserByEmail(principal.getName());  // Assuming you have a method to fetch user by email
        return user
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with email {} not found", principal.getName());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
    }


    // ✅ Test Access (For authentication verification)
    @GetMapping("/test-access")
    public ResponseEntity<?> testAccess(Principal principal) {
        return ResponseEntity.ok("You are: " + principal.getName());
    }

    // ✅ Public Registration (CUSTOMER only)
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody @Validated RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());
        // Add basic validation (e.g., check for empty fields) before proceeding
        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        User registeredUser = userService.registerUser(user, request.getBranchId());
        return ResponseEntity.ok(registeredUser);
    }

    // ✅ Get all users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        log.info("Fetching user with ID: {}", id);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // ✅ Get users by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<User>> getUsersByStatus(@PathVariable String status) {
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            log.info("Fetching users with status: {}", accountStatus);
            return ResponseEntity.ok(userService.getUsersByStatus(accountStatus));
        } catch (IllegalArgumentException e) {
            log.error("Invalid account status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Create a user (admin/internal use)
    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestBody @Validated User user,
            @RequestParam UUID branchId
    ) {
        log.info("Creating new user (internal): {}", user.getEmail());
        User createdUser = userService.createUser(user, branchId);
        return ResponseEntity.ok(createdUser);
    }

    // ✅ Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserRequest request) {
        log.info("Updating user with ID: {}", id);
        Optional<User> updatedUser = userService.updateUserFromRequest(id, request);
        return updatedUser
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with ID {} not found for update", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // ✅ Soft delete user
    @PostMapping("/{id}/delete")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        log.info("Soft-deleting user with ID: {}", id);
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        } else {
            log.warn("User with ID {} not found for deletion", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    // ✅ Restore user
    @PostMapping("/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        log.info("Restoring user with ID: {}", id);
        boolean restored = userService.restoreUser(id);
        if (restored) {
            return ResponseEntity.ok("User restored to ACTIVE");
        } else {
            log.warn("User with ID {} not found or not in DELETED status", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not in DELETED status or does not exist.");
        }
    }
}
