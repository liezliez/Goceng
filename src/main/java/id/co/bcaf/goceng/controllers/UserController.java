package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.*;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        try {
            RegisterResponse registerResponse = userService.registerUser(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful", registerResponse));
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate entry error: {}", ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Registration failed: email or NIK already registered", null));
        } catch (Exception ex) {
            log.error("Unexpected error during registration: {}", ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Registration failed due to a server error", null));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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
            log.error("Invalid account status: {}", status);
            return ResponseEntity.badRequest().body("Invalid account status value.");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> createUserFromRequest(@RequestBody @Valid CreateUserRequest request) {
        log.info("Creating user: {}", request.getEmail());
        User createdUser = userService.createUserFromRequest(request);
        return ResponseEntity.ok(toUserResponse(createdUser));
    }

    @PutMapping("/id/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/id/{id}/edit")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<UserResponse> editUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        log.info("Editing user with ID: {}", id);
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

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

    @PutMapping("/id/{id}/delete")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @PutMapping("/id/{id}/restore")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<String> restoreUser(@PathVariable UUID id) {
        if (userService.restoreUser(id)) {
            return ResponseEntity.ok("User restored to ACTIVE");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("User is not in DELETED status or does not exist.");
    }

    @GetMapping("/count-by-status")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USERS')")
    public ResponseEntity<Map<AccountStatus, Long>> countUsersByStatus() {
        return ResponseEntity.ok(userService.countUsersGroupedByStatus());
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenRequest request) {

        if (userDetails == null) {
            System.out.println("updateFcmToken: userDetails is null - unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("updateFcmToken: user = " + userDetails.getUsername() + ", token = " + request.getFcmToken());

        Optional<User> userOpt = userService.getUserByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            System.out.println("updateFcmToken: user not found by email");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        boolean updated = userService.updateFcmToken(userOpt.get().getIdUser(), request.getFcmToken());
        if (updated) {
            System.out.println("updateFcmToken: FCM token updated successfully");
            return ResponseEntity.ok().build();
        } else {
            System.out.println("updateFcmToken: failed to update FCM token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
