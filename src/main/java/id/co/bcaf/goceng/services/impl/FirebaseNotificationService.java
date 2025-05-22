package id.co.bcaf.goceng.services.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService implements NotificationService {

    @Override
    public void sendAppNotification(User user, String title, String message) {
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isEmpty()) {
            return; // no token to send
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build();

        Message msg = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(msg);
            System.out.println("Notification sent: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
