package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.AuthRequest;
import id.co.bcaf.goceng.dto.AuthResponse;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.UserRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    public AuthResponse authenticateUser(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        logger.info("Attempting login for email: {}", email);

        // Check if the user exists with the given email
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Perform password matching
            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("Login successful for email: {}", email);
                String token = jwtUtil.generateToken(email);
                return new AuthResponse(token);
            }
        }

        // Always log an unauthorized error with a generic message to prevent revealing which part of login failed
        logger.warn("Failed login attempt for email: {}", email);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials");
    }
}
