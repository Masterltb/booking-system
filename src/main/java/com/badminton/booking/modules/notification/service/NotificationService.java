package com.badminton.booking.modules.notification.service;

import com.badminton.booking.modules.notification.dto.NotificationResponse;
import com.badminton.booking.modules.notification.entity.Notification;
import com.badminton.booking.modules.notification.repository.NotificationRepository;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Gửi thông báo cho User cụ thể.
     * Topic: /queue/user/{userId}
     */
    @Transactional
    public void sendToUser(Long userId, String title, String message, String type) {
        // 1. Lưu vào DB
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);

            // 2. Đẩy qua WebSocket
            NotificationResponse response = new NotificationResponse(
                    notification.getId(), title, message, type, false, notification.getCreatedAt()
            );
            messagingTemplate.convertAndSend("/queue/user/" + userId, response);
        }
    }

    /**
     * Gửi thông báo cho Owner (Thực chất cũng là User).
     * Topic: /queue/owner/{ownerId}
     */
    @Transactional
    public void sendToOwner(Long ownerId, String title, String message, String type) {
        // Logic giống sendToUser nhưng topic khác để Frontend dễ phân biệt
        User user = userRepository.findById(ownerId).orElse(null);
        if (user != null) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);

            NotificationResponse response = new NotificationResponse(
                    notification.getId(), title, message, type, false, notification.getCreatedAt()
            );
            messagingTemplate.convertAndSend("/queue/owner/" + ownerId, response);
        }
    }
}
