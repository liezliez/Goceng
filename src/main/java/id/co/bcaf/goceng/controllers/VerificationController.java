package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.models.VerificationToken;
import id.co.bcaf.goceng.repositories.UserRepository;
import id.co.bcaf.goceng.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class VerificationController {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint to verify a user's email by validating the verification token.
     *
     * @param token The verification token sent to user's email.
     * @return ResponseEntity with status and message indicating success or failure.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        // Find the verification token by its token string
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            // Token not found - return bad request
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }

        VerificationToken verificationToken = optionalToken.get();

        // Check if token is expired
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired.");
        }

        // Get the user associated with the token
        User user = verificationToken.getUser();

        // Set user account status to ACTIVE to indicate verified email
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Save updated user status in the database
        userRepository.save(user);

        // Delete the token after successful verification (optional cleanup)
        tokenRepository.delete(verificationToken);

        // Return success response
        return ResponseEntity.ok("Email verified successfully!");
    }
}
