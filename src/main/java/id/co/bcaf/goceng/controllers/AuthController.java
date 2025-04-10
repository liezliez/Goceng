package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.AuthRequest;
import id.co.bcaf.goceng.dto.AuthResponse;
import id.co.bcaf.goceng.services.AuthService;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Password reset
import id.co.bcaf.goceng.services.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.authenticateUser(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                return ResponseEntity.ok("Email: " + jwtUtil.extractEmail(token));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
    }

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            passwordResetService.sendResetEmail(email);
            return ResponseEntity.ok("Reset link sent to your email.");
        } catch (Exception ex) {
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
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "data", null
            ));
        }
    }



}