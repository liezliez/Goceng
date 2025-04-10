//package id.co.bcaf.goceng.services;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    public void sendResetPasswordEmail(String to, String resetLink) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(to);
//            helper.setSubject("Reset Your Goceng Password");
//            helper.setText("Click the link to reset your password: " + resetLink, true);
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send email", e);
//        }
//    }
//}
