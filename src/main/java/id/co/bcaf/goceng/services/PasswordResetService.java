package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.PasswordResetToken;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.PasswordResetTokenRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final String SMTP_HOST = "smtp.mailtrap.io";
    private static final int SMTP_PORT = 2525;
    private static final String MAIL_FROM = "no-reply@goceng.co.id";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${mailtrap.username}")
    private String mailUsername;

    @Value("${mailtrap.password}")
    private String mailPassword;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void sendResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
        if (existingToken.isPresent() && existingToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("A reset token has already been issued. Please check your email.");
        }

        tokenRepository.deleteByUser(user);

        String token = generateSecureToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        tokenRepository.save(resetToken);

        sendEmail(email, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.deleteById(resetToken.getId());
    }

    private void sendEmail(String to, String token) {
        String subject = "Reset Your Password";
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;
        String body = """
                <html>
                <body>
                    <p>Dear User,</p>
                    <p>To reset your password, click the link below:</p>
                    <p><a href="%s">%s</a></p>
                    <p>This link will expire in %d minutes.</p>
                </body>
                </html>
                """.formatted(resetUrl, resetUrl, TOKEN_EXPIRY_MINUTES);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props, null);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message, mailUsername, mailPassword);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send reset email", e);
        }
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
