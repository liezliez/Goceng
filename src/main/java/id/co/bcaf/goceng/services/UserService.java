package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    public User registerUser(User user) {
        Role defaultRole = roleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));
        user.setRole(defaultRole);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createUser(User user) {
        Role role = roleRepository.findById(user.getRole().getId_role())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
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
