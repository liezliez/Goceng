package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.EmailService;
import id.co.bcaf.goceng.services.impl.FirebaseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for testing notification-related services.
 *
 * Provides endpoints to:
 * - Send a test email via {@link #sendTestEmail(String)}
 * - Send a test push notification via {@link #sendTestNotification(String)}
 *
 * These endpoints help verify that email and Firebase notification services
 * are correctly configured and functional.
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestNotificationController {

    private final EmailService emailService;
    private final FirebaseNotificationService firebaseNotificationService;

    @PostMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String toEmail) {
        String subject = "Test Email from Goceng App";
        String body = "This is a test email sent via the EmailService.";
        emailService.sendEmail(toEmail, subject, body);
        return ResponseEntity.ok("Email sent to " + toEmail);
    }

    @PostMapping("/send-notification")
    public ResponseEntity<String> sendTestNotification(@RequestParam String fcmToken) {
        String title = "Test Notification";
        String body = "This is a test push notification from Goceng backend.";
        boolean success = firebaseNotificationService.sendNotification(fcmToken, title, body);

        if (success) {
            return ResponseEntity.ok("Notification sent successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to send notification");
        }
    }
}
