package com.badminton.booking.modules.notification.repository;

import com.badminton.booking.modules.notification.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Lấy danh sách review của một Venue.
     */
    Page<Review> findByVenueIdOrderByCreatedAtDesc(Long venueId, Pageable pageable);

    /**
     * Kiểm tra xem booking này đã được review chưa.
     * Tránh spam review.
     */
    boolean existsByBookingId(Long bookingId);

    /**
     * Tính điểm trung bình và tổng số review của một Venue.
     * Trả về Object[]: [0] = average, [1] = count
     */
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.venue.id = :venueId")
    Object[] calculateRatingStats(@Param("venueId") Long venueId);
}
