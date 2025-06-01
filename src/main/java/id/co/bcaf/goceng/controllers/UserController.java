package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.*;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the currently authenticated user based on the principal
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserByPrincipal(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.getUserByEmail(principal.getName())
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest request) {
        try {
            RegisterResponse registerResponse = userService.registerUser(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful", registerResponse));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Registration failed: email or NIK already registered", null));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Registration failed due to a server error", null));
        }
    }

    /**
     * Get all users (requires VIEW_USERS permission)
     */
    @GetMapping("/list")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by UUID (requires VIEW_USERS permission)
     */
    @GetMapping("/id/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by email (requires VIEW_USERS permission)
     */
    @GetMapping("/email")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get users filtered by account status (requires VIEW_USERS permission)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status) {
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            List<UserResponse> users = userService.getUsersByStatus(accountStatus)
                    .stream()
                    .map(this::toUserResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid account status value.");
        }
    }

    /**
     * Create a new user (requires MANAGE_USERS permission)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> createUserFromRequest(@RequestBody @Valid CreateUserRequest request) {
        User createdUser = userService.createUserFromRequest(request);
        return ResponseEntity.ok(toUserResponse(createdUser));
    }

    /**
     * Update an existing user by ID (requires MANAGE_USERS permission)
     */
    @PutMapping("/id/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Edit an existing user by ID (requires MANAGE_USERS permission)
     */
    @PutMapping("/id/{id}/edit")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> editUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Change password for the authenticated user (requires CHANGE_PASSWORD permission)
     */
    @PutMapping("/change-password")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('CHANGE_PASSWORD')")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            boolean result = userService.changePassword(
                    userDetails.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            if (result) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Old password is incorrect or user not found", null));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Soft delete a user by ID (requires MANAGE_USERS permission)
     */
    @PutMapping("/id/{id}/delete")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    /**
     * Restore a soft-deleted user by ID (requires MANAGE_USERS permission)
     */
    @PutMapping("/id/{id}/restore")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        if (userService.restoreUser(id)) {
            return ResponseEntity.ok("User restored to ACTIVE");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("User is not in DELETED status or does not exist.");
    }

    /**
     * Count users grouped by their account status (requires VIEW_USERS permission)
     */
    @GetMapping("/count-by-status")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<Map<AccountStatus, Long>> countUsersByStatus() {
        return ResponseEntity.ok(userService.countUsersGroupedByStatus());
    }

    /**
     * Update the FCM token for the authenticated user
     */
    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = userService.getUserByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        boolean updated = userService.updateFcmToken(userOpt.get().getIdUser(), request.getFcmToken());
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getIdUser());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAccount_status(user.getStatus());

        if (user.getRole() != null) {
            UserResponse.RoleDto roleDto = new UserResponse.RoleDto();
            roleDto.setId(user.getRole().getIdRole());
            roleDto.setRoleName(user.getRole().getRoleName());
            dto.setRole(roleDto);
        } else {
            dto.setRole(null);
        }

        if (user.getBranch() != null) {
            UserResponse.BranchDto branchDto = new UserResponse.BranchDto();
            branchDto.setId(user.getBranch().getId());
            branchDto.setName(user.getBranch().getName());
            dto.setBranch(branchDto);
        } else {
            dto.setBranch(null);
        }

        if (user.getEmployee() != null) {
            UserResponse.EmployeeDto employeeDto = new UserResponse.EmployeeDto();
            employeeDto.setId(user.getEmployee().getId());
            employeeDto.setName(user.getEmployee().getName());
            dto.setEmployee(employeeDto);
        } else {
            dto.setEmployee(null);
        }

        return dto;
    }
}
