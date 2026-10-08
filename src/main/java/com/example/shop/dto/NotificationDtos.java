package com.example.shop.dto;

import com.example.shop.model.Notification;
import com.example.shop.model.NotificationType;

import java.time.LocalDateTime;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationResponse(Long id, NotificationType type, String title,
                                       String message, boolean seen, LocalDateTime createdAt) {
        public static NotificationResponse from(Notification notification) {
            return new NotificationResponse(notification.getId(), notification.getType(),
                    notification.getTitle(), notification.getMessage(), notification.isSeen(),
                    notification.getCreatedAt());
        }
    }
}
