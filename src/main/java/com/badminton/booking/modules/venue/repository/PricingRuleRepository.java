package com.badminton.booking.modules.venue.repository;

import com.badminton.booking.modules.venue.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    /**
     * Lấy bảng giá của một Venue.
     * Sắp xếp theo độ ưu tiên (priority) giảm dần để dễ tính toán giá logic.
     * (Rule nào priority cao hơn sẽ được áp dụng trước).
     */
    @Query("SELECT p FROM PricingRule p WHERE p.venue.id = :venueId " +
           "ORDER BY p.priority DESC")
    List<PricingRule> findByVenueIdOrderByPriorityDesc(@Param("venueId") Long venueId);

    /**
     * Kiểm tra trùng lặp khung giờ giá (Pricing Overlap).
     * Logic: (StartA < EndB) AND (EndA > StartB).
     * Áp dụng cho cùng 1 Venue và cùng 1 Court (hoặc cả 2 đều null court).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PricingRule p " +
           "WHERE p.venue.id = :venueId " +
           "AND (:courtId IS NULL OR p.court.id = :courtId) " +
           "AND p.startTime < :endTime " +
           "AND p.endTime > :startTime")
    boolean existsByOverlapTime(
            @Param("venueId") Long venueId,
            @Param("courtId") Long courtId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
