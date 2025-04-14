package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.BranchRepository;  // Assuming you have a BranchRepository
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;  // Inject the BranchRepository
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
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

    public User registerUser(User user, UUID branchId) {
        Role defaultRole = roleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        user.setRole(defaultRole);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setBranch(branch);  // Assign the branch to the user

        return userRepository.save(user);
    }

    public User createUser(User user, UUID branchId) {
        // Fetch the role
        Role role = roleRepository.findById(user.getRole().getId_role())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Fetch the branch
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBranch(branch);  // Assign the branch to the user

        return saveUser(user);
    }

    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getRole() != null) {
                Role role = roleRepository.findById(userDetails.getRole().getId_role())
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                user.setRole(role);
            }
            user.setEmail(userDetails.getEmail());
            user.setName(userDetails.getName());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            user.setAccountStatus(userDetails.getAccountStatus());
            if (userDetails.getBranch() != null) {
                user.setBranch(userDetails.getBranch());  // Update the branch if provided
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
}
