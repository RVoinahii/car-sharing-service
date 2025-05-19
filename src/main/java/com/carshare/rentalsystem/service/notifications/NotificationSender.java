package com.carshare.rentalsystem.service.notifications;

public interface NotificationSender<T> {
    void sendNotification(NotificationType type, T notificationData, Long userId);
}
