package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.User;

public interface NotificationService {
    void sendAppNotification(User user, String title, String message);
}
