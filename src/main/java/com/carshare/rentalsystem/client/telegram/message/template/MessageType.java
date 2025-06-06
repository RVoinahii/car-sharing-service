package com.carshare.rentalsystem.client.telegram.message.template;

import com.carshare.rentalsystem.service.notifications.NotificationType;

public enum MessageType {
    COMMON_AUTH_LINK_MSG,
    COMMON_HELP_MSG,
    RENTAL_INFO_MSG,
    RENTAL_LIST_MSG,
    RENTAL_NEW_MSG,
    RENTAL_RETURN_MSG,
    RENTAL_START_MSG,
    RENTAL_DUE_SOON_MSG,
    RENTAL_OVERDUE_MSG,
    PAYMENT_INFO_MSG,
    PAYMENT_LIST_MSG,
    PAYMENT_SUCCESS_MSG,
    PAYMENT_CANCEL_MSG,
    PAYMENT_RENEW_MSG,
    PAYMENT_EXPIRED_MSG;

    public static MessageType mapNotificationToMessageType(NotificationType notificationType) {
        return switch (notificationType) {
            case RENTAL_NEW_NOTIF -> RENTAL_NEW_MSG;
            case RENTAL_RETURN_NOTIF -> RENTAL_RETURN_MSG;
            case RENTAL_START_NOTIF -> RENTAL_START_MSG;
            case RENTAL_DUE_SOON_NOTIF -> RENTAL_DUE_SOON_MSG;
            case RENTAL_OVERDUE_NOTIF -> RENTAL_OVERDUE_MSG;
            case PAYMENT_SUCCESS_NOTIF -> PAYMENT_SUCCESS_MSG;
            case PAYMENT_CANCEL_NOTIF -> PAYMENT_CANCEL_MSG;
            case PAYMENT_RENEW_NOTIF -> PAYMENT_RENEW_MSG;
            case PAYMENT_EXPIRED_NOTIF -> PAYMENT_EXPIRED_MSG;
        };
    }
}
