package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByStatus(AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        Integer roleId = user.getRole().getId_role();
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    public Optional<User> updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            Integer roleId = userDetails.getRole().getId_role();
            Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
            user.setEmail(userDetails.getEmail());
            user.setName(userDetails.getName());
            user.setPassword(userDetails.getPassword());
            user.setAccountStatus(userDetails.getAccountStatus()); 
            return userRepository.save(user);
        });
    }

    // # Soft Delete: Change account_status to DELETED instead of removing user from DB
    public boolean deleteUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setAccountStatus(AccountStatus.DELETED); 
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    // # Restore User: Change account_status from DELETED to ACTIVE
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
