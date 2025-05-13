package com.carshare.rentalsystem.service.notifications;

public interface NotificationSender {
    void sendNotification(NotificationType type, Object notificationData, Long userId);
}
