package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.BranchRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all users (only ADMIN, BRANCH_MANAGER, BACK_OFFICE can access)
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('BACK_OFFICE')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get users by account status
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('BACK_OFFICE')")
    public List<User> getUsersByStatus(AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    // Get a user by their UUID (only ADMIN, BRANCH_MANAGER, BACK_OFFICE can access)
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('BACK_OFFICE')")
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    // Get a user by email
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('BACK_OFFICE')")
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Register a new user
    public User registerUser(User user, UUID branchId) {
        Role defaultRole = getRoleById(2); // Default role as 'CUSTOMER' (id_role = 2)
        Branch branch = getBranchById(branchId);

        user.setRole(defaultRole);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setBranch(branch);

        return userRepository.save(user);
    }


    // Create a new user
    public User createUser(User user, UUID branchId) {
        Role role = getRoleById(user.getRole().getId_role());
        Branch branch = getBranchById(branchId);

        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBranch(branch);

        return saveUser(user);
    }

    // Helper method to save a user
    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Update a user's details
    public Optional<User> updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getRole() != null) {
                Role role = getRoleById(userDetails.getRole().getId_role());
                user.setRole(role);
            }

            user.setEmail(userDetails.getEmail());
            user.setName(userDetails.getName());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            user.setAccountStatus(userDetails.getAccountStatus());

            if (userDetails.getBranch() != null) {
                user.setBranch(userDetails.getBranch()); // Update the branch if provided
            }

            return userRepository.save(user);
        });
    }

    // Soft delete a user (set status to DELETED)
    public boolean deleteUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setAccountStatus(AccountStatus.DELETED);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    // Restore a deleted user (set status to ACTIVE)
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

    // Fetch a Role by ID, or throw an exception if not found
    private Role getRoleById(int roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role with ID " + roleId + " not found"));
    }

    // Fetch a Branch by ID, or throw an exception if not found
    private Branch getBranchById(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch with ID " + branchId + " not found"));
    }
}
