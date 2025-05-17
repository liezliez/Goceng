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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.JwtException;
import id.co.bcaf.goceng.services.PasswordResetService;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate user
            AuthResponse authResponse = authService.authenticateUser(authRequest);

            // Check if the user is authenticated (after login)
            boolean isAuthenticated = authService.isUserAuthenticated();
            if (!isAuthenticated) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
            }

            // Log the authentication details
            log.info("User authenticated successfully. Email: {}", authRequest.getEmail());
            log.info("Authentication details: Token: {}", authResponse.getToken());
            log.info("Token Type: Bearer");
            log.info("Token Expiration: {}", jwtUtil.extractExpiration(authResponse.getToken()));

            // Return auth response with token
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Login failed for user: {}", authRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or authentication error.");
        }
    }


    // Test the token
    @GetMapping("/test")
    public ResponseEntity<String> testToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        log.info("RAW AUTH HEADER: '{}'", authHeader);
        log.info("EXTRACTED TOKEN: '{}'", token);
        if (token != null) {
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
        String token = extractTokenFromHeader(request.getHeader("Authorization"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Token is expired: {}", token);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token is expired. Please log in again.");
            }

            // Check if the token is already blacklisted
            if (blacklistedTokenRepository.existsByToken(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token has already been blacklisted.");
            }

            // Blacklist the token
            BlacklistedToken blacklisted = new BlacklistedToken();
            blacklisted.setToken(token);
            blacklisted.setExpiryDate(
                    jwtUtil.extractExpiration(token).toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
            blacklistedTokenRepository.saveAndFlush(blacklisted);

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

    // Helper method for token extraction
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    @GetMapping("/check-authorities")
    public ResponseEntity<Map<String, Object>> checkAuthorities(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Extract JWT token from Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        Map<String, Object> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("authorities", authorityList);
        response.put("authorizationHeader", authorizationHeader);

        // Optionally, if you want to return all headers as a Map<String, String>
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headersMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headersMap.put(headerName, request.getHeader(headerName));
        }
        response.put("allHeaders", headersMap);

        return ResponseEntity.ok(response);
    }

}
