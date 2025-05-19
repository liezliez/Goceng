package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.RegisterRequest;
import id.co.bcaf.goceng.dto.RoleDto;
import id.co.bcaf.goceng.dto.UserRequest;
import id.co.bcaf.goceng.dto.UserResponse;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserByPrincipal(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.getUserByEmail(principal.getName())
                .map(user -> toUserResponse(user))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/test-access")
    public ResponseEntity<String> testAccess(Principal principal) {
        return ResponseEntity.ok("You are: " + principal.getName());
    }

    @PutMapping("/test-put")
    public ResponseEntity<String> testPut() {
        return ResponseEntity.ok("PUT works");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null || request.getNik() == null) {
            return ResponseEntity.badRequest().body("Missing required fields.");
        }

        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setName(request.getName());
            userRequest.setEmail(request.getEmail());
            userRequest.setPassword(request.getPassword());
            userRequest.setNik(request.getNik());  // add this line!

            UserResponse registeredUserResponse = userService.registerUser(userRequest);

            return ResponseEntity.ok(registeredUserResponse);
        } catch (Exception ex) {
            log.error("Error during registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed.");
        }
    }



    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
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
//    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid User user, @RequestParam UUID branchId) {
        log.info("Creating user: {}", user.getEmail());
        User createdUser = userService.createUser(user, branchId);
        return ResponseEntity.ok(toUserResponse(createdUser));
    }

    @PutMapping("/id/{id}")
//    @PreAuthorize("hasAuthority('EDIT_USER')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/id/{id}/edit")
//    @PreAuthorize("hasAuthority('EDIT_USER')")
    public ResponseEntity<UserResponse> editUser(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        log.info("Editing user with ID: {}", id);
        return userService.updateUserFromRequest(id, request)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/id/{id}/delete")
//    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<String> softDeleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User has been soft-deleted (status: DELETED)");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @PutMapping("/id/{id}/restore")
//    @PreAuthorize("hasAuthority('RESTORE_USER')")
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
            branchDto.setId(user.getBranch().getId());      // Assuming getId() returns UUID
            branchDto.setName(user.getBranch().getName());  // Assuming getName() exists
            dto.setBranch(branchDto);
        } else {
            dto.setBranch(null);
        }

        if (user.getEmployee() != null) {
            UserResponse.EmployeeDto employeeDto = new UserResponse.EmployeeDto();
            employeeDto.setId(user.getEmployee().getId());     // Assuming getId() returns UUID
            employeeDto.setName(user.getEmployee().getName()); // Assuming getName() exists
            dto.setEmployee(employeeDto);
        } else {
            dto.setEmployee(null);
        }

        return dto;
    }




}
