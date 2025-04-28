package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.AuthRequest;
import id.co.bcaf.goceng.dto.AuthResponse;
import id.co.bcaf.goceng.models.BlacklistedToken;
import id.co.bcaf.goceng.repositories.BlacklistedTokenRepository;
import id.co.bcaf.goceng.services.AuthService;
import id.co.bcaf.goceng.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.JwtException;

import id.co.bcaf.goceng.services.PasswordResetService;

import java.time.ZoneId;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService;
    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;
    @Autowired
    private PasswordResetService passwordResetService;

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUser(authRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Login failed for user: {}", authRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or authentication error.");
        }
    }

    // Test the token
    @GetMapping("/test")
    public ResponseEntity<String> testToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                String email = jwtUtil.extractEmail(token);
                return ResponseEntity.ok("Email: " + email);
            } catch (ExpiredJwtException e) {
                log.error("Token expired", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired token: " + e.getMessage());
            } catch (MalformedJwtException e) {
                log.error("Malformed token", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed token: " + e.getMessage());
            } catch (JwtException e) {
                log.error("JWT error", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
    }

    // Password Reset
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            passwordResetService.sendResetEmail(email);
            return ResponseEntity.ok("Reset link sent to your email.");
        } catch (Exception ex) {
            log.error("Error sending password reset email", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "data", null
            ));
        }
    }

    // Logout and blacklist the token
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        log.info("Logout called. Authorization header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            log.info("Extracted token for blacklisting: {}", token);

            // Check if the token is expired or valid before processing
            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("Token is expired: {}", token);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Token is expired. Please log in again.");
                }

                // If token is valid, blacklist it
                BlacklistedToken blacklisted = new BlacklistedToken();
                blacklisted.setToken(token);
                blacklisted.setExpiryDate(
                        jwtUtil.extractExpiration(token).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                );
                blacklistedTokenRepository.saveAndFlush(blacklisted); // Combine save + flush

                log.info("Token has been blacklisted successfully.");
                return ResponseEntity.ok("Logged out successfully and token has been blacklisted.");
            } catch (ExpiredJwtException e) {
                log.error("Expired token during logout", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired.");
            } catch (MalformedJwtException e) {
                log.error("Malformed token during logout", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed token.");
            } catch (JwtException e) {
                log.error("JWT exception during logout", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
            } catch (Exception e) {
                log.error("Unexpected error during logout", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while logging out.");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
    }
}
