package com.badminton.booking.modules.notification.service.impl;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.notification.dto.ReviewRequest;
import com.badminton.booking.modules.notification.entity.Review;
import com.badminton.booking.modules.notification.repository.ReviewRepository;
import com.badminton.booking.modules.notification.service.IReviewService;
import com.badminton.booking.modules.venue.entity.Venue;
import com.badminton.booking.modules.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public void createReview(ReviewRequest request) {
        // 1. Lấy User
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Lấy Booking
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.bookingId()));

        // 3. Validate: Phải là booking của user này
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AppException("Bạn không có quyền đánh giá booking này");
        }

        // 4. Validate: Trạng thái phải là COMPLETED hoặc CHECKED_IN
        if (booking.getStatus() != BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new AppException("Bạn chỉ được đánh giá sau khi đã sử dụng sân");
        }

        // 5. Validate: Chưa review lần nào
        if (reviewRepository.existsByBookingId(request.bookingId())) {
            throw new ConflictException("Bạn đã đánh giá booking này rồi");
        }

        // 6. Lưu Review
        Review review = Review.builder()
                .user(user)
                .booking(booking)
                .venue(booking.getCourt().getVenue())
                .rating(request.rating())
                .comment(request.comment())
                .build();
        reviewRepository.save(review);

        // 7. Tính lại điểm trung bình cho Venue (Denormalization)
        updateVenueRating(booking.getCourt().getVenue().getId());
    }

    @Override
    public Page<Review> getVenueReviews(Long venueId, Pageable pageable) {
        return reviewRepository.findByVenueIdOrderByCreatedAtDesc(venueId, pageable);
    }

    private void updateVenueRating(Long venueId) {
        Object[] stats = (Object[]) reviewRepository.calculateRatingStats(venueId)[0];
        Double avgRating = (Double) stats[0];
        Long totalReviews = (Long) stats[1];

        Venue venue = venueRepository.findById(venueId).orElseThrow();
        venue.setAverageRating(avgRating != null ? avgRating : 0.0);
        venue.setTotalReviews(totalReviews.intValue());
        venueRepository.save(venue);
    }
}
