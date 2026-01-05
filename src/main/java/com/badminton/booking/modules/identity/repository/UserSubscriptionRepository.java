package com.badminton.booking.modules.identity.repository;

import com.badminton.booking.modules.identity.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * Tìm gói đăng ký đang hoạt động của User.
     * Logic: Status là ACTIVE và chưa hết hạn (endDate > now).
     */
    @Query("SELECT us FROM UserSubscription us " +
           "WHERE us.user.id = :userId " +
           "AND us.status = 'ACTIVE' " +
           "AND us.endDate > :now")
    Optional<UserSubscription> findActiveSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Tính tổng doanh thu từ việc bán gói Subscription.
     * Dùng cho Admin Dashboard.
     */
    @Query("SELECT SUM(us.plan.price) FROM UserSubscription us " +
           "WHERE us.status = 'ACTIVE' " +
           "AND us.startDate BETWEEN :startDate AND :endDate")
    BigDecimal sumSubscriptionRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}
