package com.badminton.booking.modules.notification.repository;

import com.badminton.booking.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lấy thông báo của user.
     * Ưu tiên thông báo chưa đọc lên đầu.
     */
    Page<Notification> findByUserIdOrderByIsReadAscCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Đếm số lượng thông báo chưa đọc (để hiện badge đỏ trên app).
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Đánh dấu tất cả là đã đọc.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);
}
