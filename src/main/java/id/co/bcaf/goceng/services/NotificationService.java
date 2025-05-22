package id.co.bcaf.goceng.services;

public interface NotificationService {
    boolean sendNotification(String fcmToken, String title, String body);
}
