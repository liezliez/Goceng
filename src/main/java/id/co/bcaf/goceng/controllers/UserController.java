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
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

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

    // # Fetch users by status (ACTIVE, BANNED, DELETED)
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

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating new user: {}", user.getEmail());
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        log.info("Updating user with ID: {}", id);
        return userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with ID {} not found for update", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        log.info("Soft-deleting user with ID: {}", id);
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }


    @PostMapping("/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        log.info("Restoring user with ID: {}", id);
        boolean restored = userService.restoreUser(id);
        if (restored) {
            return ResponseEntity.ok("User: restored to ACTIVE");
        }
        log.warn("User with ID {} not found or not in DELETED status", id);
        return ResponseEntity.badRequest().body("User is not in DELETED status or does not exist.");
    }

}
