package id.co.bcaf.goceng.services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
