package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.AuthRequest;
import id.co.bcaf.goceng.dto.AuthResponse;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.UserRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleFeatureService roleFeatureService;

    public AuthResponse authenticateUser(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        // Retrieve user by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        // If user not found, deny access
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        User user = userOptional.get();

        // Verify password correctness
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials");
        }

        // Restrict login if account status is BANNED or DELETED
        if (user.getAccountStatus() == AccountStatus.BANNED || user.getAccountStatus() == AccountStatus.DELETED) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please contact goceng Customer Service at 1500666"
            );
        }

        // Prepare JWT token and related user information
        String roleName = user.getRole().getRoleName();
        Long roleId = Long.valueOf(user.getRole().getIdRole());

        String token = jwtUtil.generateToken(email, roleName);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        Long expiresAt = jwtUtil.extractExpiration(token).getTime().getTime();

        // Fetch role-specific features/permissions
        List<String> userFeatures = roleFeatureService.getFeaturesByRoleId(roleId);

        // Return authentication response containing JWT and user info
        return new AuthResponse(token, refreshToken, user.getUsername(), expiresAt, userFeatures);
    }


    public boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

}
