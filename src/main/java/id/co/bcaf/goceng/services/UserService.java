package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.*;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.BranchRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;

    @Autowired
    private CustomerService customerService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder, EmployeeService employeeService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeService = employeeService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByStatus(AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }



    public RegisterResponse registerUser(RegisterRequest request) {
        Role defaultRole = getRoleById(2); // Default Role Customer

        User user = new User();
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRole(defaultRole);
        if (request.getId_branch() != null) {
            Branch branch = getBranchById(request.getId_branch());
            user.setBranch(branch);
        }
        User savedUser = userRepository.save(user);
        CustomerResponse customerResponse = customerService.createCustomerFromUser(savedUser, request.getName(), request.getNik());
        UserResponse userResponse = mapToUserResponse(savedUser);
        return new RegisterResponse(userResponse, customerResponse);
    }


    @Transactional
    public User createUserFromRequest(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
        user.setBranch(branch);
        Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        user.setRole(role);
        user = userRepository.save(user);

        // Create employee for user
        employeeService.createEmployee(user.getIdUser(), role.getIdRole());

        return user;
    }

    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public Optional<User> updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getRole() != null) {
                Role role = getRoleById(userDetails.getRole().getIdRole());
                user.setRole(role);
            }
            user.setEmail(userDetails.getEmail());
            user.setName(userDetails.getName());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            user.setAccountStatus(userDetails.getAccountStatus());

            if (userDetails.getBranch() != null) {
                user.setBranch(userDetails.getBranch());
            }

            return userRepository.save(user);
        });
    }

    public boolean deleteUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setAccountStatus(AccountStatus.DELETED);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public boolean restoreUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            if (user.getAccountStatus() == AccountStatus.DELETED) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }

    private Role getRoleById(int roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role with ID " + roleId + " not found"));
    }

    private Branch getBranchById(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch with ID " + branchId + " not found"));
    }

    public Map<AccountStatus, Long> countUsersGroupedByStatus() {
        return userRepository.findAll().stream()
                .collect(Collectors.groupingBy(User::getAccountStatus, Collectors.counting()));
    }

    public Optional<User> updateUserFromRequest(UUID id, UserRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getAccount_status() != null) user.setAccountStatus(request.getAccount_status());

            if (request.getIdRole() != null) {
                Role role = getRoleById(request.getIdRole());
                user.setRole(role);
            }

            if (request.getId_branch() != null) {
                Branch branch = getBranchById(request.getId_branch());
                user.setBranch(branch);
            }

            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            return userRepository.save(user);
        });
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        return userRepository.findByEmail(email).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false; // old password does not match
            }

            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                // new password is same as current
                throw new IllegalArgumentException("New password must be different from the current password");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAccount_status(user.getAccountStatus());

        // Map Role to RoleDto
        if (user.getRole() != null) {
            UserResponse.RoleDto roleDto = new UserResponse.RoleDto();
            roleDto.setId(user.getRole().getIdRole());          // Integer id
            roleDto.setRoleName(user.getRole().getRoleName());
            response.setRole(roleDto);
        }

        // Map Branch to BranchDto
        if (user.getBranch() != null) {
            UserResponse.BranchDto branchDto = new UserResponse.BranchDto();
            branchDto.setId(user.getBranch().getId());    // UUID id
            branchDto.setName(user.getBranch().getName());
            response.setBranch(branchDto);
        }

        // Map Employee to EmployeeDto if applicable
        if (user.getEmployee() != null) {
            UserResponse.EmployeeDto employeeDto = new UserResponse.EmployeeDto();
            employeeDto.setId(user.getEmployee().getId()); // UUID id
            employeeDto.setName(user.getEmployee().getName());
            response.setEmployee(employeeDto);
        }

        return response;
    }

    public boolean updateFcmToken(UUID userId, String fcmToken) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFcmToken(fcmToken);
            userRepository.save(user);
            return true;
        }
        return false;
    }


}
