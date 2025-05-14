package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.UserRequest;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder) {
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
        Role defaultRole = getRoleById(2); // Default: ROLE_CUSTOMER
        Branch branch = getBranchById(branchId);

        user.setRole(defaultRole);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setBranch(branch);

        return userRepository.save(user);
    }

    public User createUser(User user, UUID branchId) {
        Role role = getRoleById(user.getRole().getIdRole());
        Branch branch = getBranchById(branchId);

        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBranch(branch);

        return saveUser(user);
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
                .collect(Collectors.groupingBy(User::getStatus, Collectors.counting()));
    }

//    public Optional<User> updateUserFromRequest(UUID id, UserRequest request) {
//        return userRepository.findById(id).map(user -> {
//            if (request.getName() != null) user.setName(request.getName());
//            if (request.getEmail() != null) user.setEmail(request.getEmail());
//            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
//                user.setPassword(passwordEncoder.encode(request.getPassword()));
//            }
//            if (request.getAccount_status() != null) {
//                user.setAccountStatus(request.getAccount_status());
//            }
//            if (request.getIdRole() != null) {
//                Role role = getRoleById(request.getIdRole());
//                user.setRole(role);
//            }
//            if (request.getIdBranch() != null) {
//                Branch branch = getBranchById(request.getIdBranch());
//                user.setBranch(branch);
//            }
//
//            return userRepository.save(user);
//        });
//    }

    public Optional<User> updateUserFromRequest(UUID id, UserRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getAccount_status() != null) {
                user.setAccountStatus(request.getAccount_status());
            }
            if (request.getIdRole() != null) {
                Role role = getRoleById(request.getIdRole());
                user.setRole(role);
            }
            if (request.getIdBranch() != null) {
                Branch branch = getBranchById(request.getIdBranch());
                user.setBranch(branch);
            }

            // Only update password if it's provided in the request
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            return userRepository.save(user);
        });
    }

    // ✅ Change password method
    public boolean changePassword(UUID userId, String oldPassword, String newPassword) {
        return userRepository.findById(userId).map(user -> {
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }


}
