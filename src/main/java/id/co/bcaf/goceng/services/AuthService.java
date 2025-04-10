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
    private PasswordEncoder passwordEncoder; // ✅ Added for password hashing

    public AuthResponse authenticateUser(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        logger.info("Attempting login for email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.info("User found: {}", user.getEmail());

            logger.info("Raw Password: {}", password);
            logger.info("Stored Hashed Password: {}", user.getPassword());

            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("Password matches. Generating token...");
                String token = jwtUtil.generateToken(email);
                return new AuthResponse(token);
            } else {
                logger.warn("Incorrect password for email: {}", email);
            }
        } else {
            logger.warn("User with email {} not found", email);
        }


        // ✅ Throw exception for unauthorized access (instead of returning null)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
