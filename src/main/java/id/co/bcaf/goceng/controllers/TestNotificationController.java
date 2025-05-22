package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestNotificationController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String toEmail) {
        String subject = "Test Email from Goceng App";
        String body = "This is a test email sent via the EmailService.";
        emailService.sendEmail(toEmail, subject, body);
        return ResponseEntity.ok("Email sent to " + toEmail);
    }
}
