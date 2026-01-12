package com.badminton.booking.modules.notification.service;

import com.badminton.booking.modules.notification.dto.ReviewRequest;
import com.badminton.booking.modules.notification.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviewService {
    
    /**
     * Tạo đánh giá mới.
     */
    void createReview(ReviewRequest request);

    /**
     * Lấy danh sách đánh giá của một sân.
     */
    Page<Review> getVenueReviews(Long venueId, Pageable pageable);
}
