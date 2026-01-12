package com.badminton.booking.modules.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String type,
    Boolean isRead,
    LocalDateTime createdAt
) {}
