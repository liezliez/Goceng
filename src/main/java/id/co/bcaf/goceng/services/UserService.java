package id.co.bcaf.goceng.services;

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

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public User createUser(User user, UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public Optional<User> updateUser(UUID id, User userDetails, UUID roleId) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setNip(userDetails.getNip());
            user.setPassword(userDetails.getPassword());

            Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);

            return userRepository.save(user);
        });
    }

    public boolean deleteUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }
}
