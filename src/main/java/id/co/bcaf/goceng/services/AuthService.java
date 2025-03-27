package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.UserRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public String authenticateUser(String email, String password) {
        logger.info("Login with email: {}", email);  // Cek apakah email diterima

        Optional<User> userOptional = usersRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.info("User Found: {}", user.getEmail()); // Konfirmasi user ditemukan di database

            // Langsung bandingkan dengan password di database (karena belum di-hash)
            if (user.getPassword().equals(password)) {
                logger.info("Password Match, generate token...");
                return jwtUtil.generateToken(email); // Kembalikan JWT Token
            }
            else {
                logger.warn("Password didn't match: {}", email);
            }
        }
        else {
            logger.warn("User with email {} not found in database", email);
        }
        return null; // Jika gagal login
    }
}