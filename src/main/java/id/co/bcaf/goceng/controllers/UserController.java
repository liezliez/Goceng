package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.ApiResponse;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")  // Allow cross-origin requests (optional)
public class UserController {

    @Autowired
    private UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", savedUser));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "User found", value)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null)));
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Optional<User>>> updateUser(@PathVariable UUID id, @Valid @RequestBody User updatedUser) {
        return userService.getUserById(id)
                .map(existingUser -> {
                    Optional<User> user = userService.updateUser(id, updatedUser);
                    return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", user));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null)));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
        }
        return ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null));
    }
}
