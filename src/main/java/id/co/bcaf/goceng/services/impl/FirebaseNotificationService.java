package id.co.bcaf.goceng.services.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import id.co.bcaf.goceng.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service  // Make sure to add this so Spring registers the bean
@Slf4j
public class FirebaseNotificationService implements NotificationService {

    @Override
    public boolean sendNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Sent message: " + response);
            return true;
        } catch (Exception e) {
            log.error("Failed to send Firebase notification", e);
            return false;
        }
    }
}
