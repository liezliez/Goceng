package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ Get all users
    @GetMapping
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

    // ✅ Get users by status (e.g. ACTIVE, DELETED, BANNED)
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

    // ✅ Create a new user with branchId as request param
    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestBody User user,
            @RequestParam UUID branchId
    ) {
        log.info("Creating new user: {}", user.getEmail());
        User createdUser = userService.createUser(user, branchId);
        return ResponseEntity.ok(createdUser);
    }

    // ✅ Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        log.info("Updating user with ID: {}", id);
        Optional<User> updatedUser = userService.updateUser(id, user);
        return updatedUser
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with ID {} not found for update", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // ✅ Soft delete user (set status to DELETED)
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

    // ✅ Restore soft-deleted user (set status to ACTIVE)
    @PostMapping("/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        log.info("Restoring user with ID: {}", id);
        boolean restored = userService.restoreUser(id);
        if (restored) {
            return ResponseEntity.ok("User: restored to ACTIVE");
        } else {
            log.warn("User with ID {} not found or not in DELETED status", id);
            return ResponseEntity.badRequest().body("User is not in DELETED status or does not exist.");
        }
    }

}
