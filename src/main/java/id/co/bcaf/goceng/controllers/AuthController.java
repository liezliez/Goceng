package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.AuthRequest;
import id.co.bcaf.goceng.dto.AuthResponse;
import id.co.bcaf.goceng.models.BlacklistedToken;
import id.co.bcaf.goceng.repositories.BlacklistedTokenRepository;
import id.co.bcaf.goceng.services.AuthService;
import id.co.bcaf.goceng.services.PasswordResetService;
import id.co.bcaf.goceng.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for handling authentication-related operations.
 *
 * Features:
 * - {@link #login(AuthRequest)}: Authenticates the user and issues a JWT token.
 * - {@link #testToken(String)}: Validates and decodes a given JWT token.
 * - {@link #forgotPassword(Map)}: Initiates the password reset process by sending a reset email.
 * - {@link #resetPassword(Map)}: Completes the password reset using a reset token and new password.
 * - {@link #logout(HttpServletRequest)}: Logs out the user and blacklists their JWT token.
 * - {@link #checkAuthorities(UserDetails, HttpServletRequest)}: Displays the user's granted authorities and headers for debugging.
 *
 * Utilizes:
 * - {@link JwtUtil} for JWT creation, parsing, and validation.
 * - {@link AuthService} for credential authentication and user session logic.
 * - {@link PasswordResetService} for password recovery flows.
 * - {@link BlacklistedTokenRepository} for token invalidation (logout).
 *
 * All routes are under `/auth`.
 */

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUser(authRequest);

            if (!authService.isUserAuthenticated()) {
                log.warn("User not authenticated after login attempt: {}", authRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
            }

            log.info("User authenticated successfully. Email: {}", authRequest.getEmail());
            log.debug("Token: {}, Type: Bearer, Expiration: {}", authResponse.getToken(), jwtUtil.extractExpiration(authResponse.getToken()));

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Login failed for user: {}", authRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or authentication error.");
        }
    }

    /**
     * Validate and test JWT token from Authorization header.
     */
    @GetMapping("/test")
    public ResponseEntity<String> testToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        log.debug("Auth Header: '{}', Token: '{}'", authHeader, token);

        if (token == null) {
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        try {
            String email = jwtUtil.extractEmail(token);
            return ResponseEntity.ok("Email: " + email);
        } catch (JwtException e) {
            return handleJwtException(e);
        } catch (Exception e) {
            log.error("Unexpected error validating token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
        }
    }

    /**
     * Initiate password reset process by sending reset email.
     */
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

    /**
     * Reset password using token and new password.
     */
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

    /**
     * Logout user by blacklisting JWT token.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = extractTokenFromHeader(request.getHeader("Authorization"));
        if (token == null) {
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Attempt to logout with expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired. Please log in again.");
            }

            if (blacklistedTokenRepository.existsByToken(token)) {
                return ResponseEntity.badRequest().body("Token has already been blacklisted.");
            }

            BlacklistedToken blacklisted = new BlacklistedToken();
            blacklisted.setToken(token);
            blacklisted.setExpiryDate(
                    jwtUtil.extractExpiration(token).toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
            blacklistedTokenRepository.saveAndFlush(blacklisted);

            log.info("Token blacklisted successfully for logout");

            return ResponseEntity.ok("Logged out successfully and token has been blacklisted.");
        } catch (JwtException e) {
            return handleJwtException(e);
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while logging out.");
        }
    }

    /**
     * Check authenticated user's authorities and return them along with headers.
     */
    @GetMapping("/check-authorities")
    public ResponseEntity<Map<String, Object>> checkAuthorities(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        List<String> authorityList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String authorizationHeader = request.getHeader("Authorization");

        Map<String, Object> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("authorities", authorityList);
        response.put("authorizationHeader", authorizationHeader);

        // Collect all headers for debugging or auditing
        Map<String, String> headersMap = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));
        response.put("allHeaders", headersMap);

        return ResponseEntity.ok(response);
    }

    // Helper: Extract token from "Bearer " Authorization header
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    // Helper: Handle JWT exceptions consistently
    private ResponseEntity<String> handleJwtException(JwtException e) {
        if (e instanceof ExpiredJwtException) {
            log.error("Token expired", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired token: " + e.getMessage());
        } else if (e instanceof MalformedJwtException) {
            log.error("Malformed token", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed token: " + e.getMessage());
        } else {
            log.error("JWT error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
        }
    }
}
