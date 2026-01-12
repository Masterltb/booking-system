package com.badminton.booking.modules.notification.controller;

import com.badminton.booking.modules.notification.dto.ReviewRequest;
import com.badminton.booking.modules.notification.entity.Review;
import com.badminton.booking.modules.notification.service.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<Void> createReview(@Valid @RequestBody ReviewRequest request) {
        reviewService.createReview(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/venues/{venueId}/reviews")
    public ResponseEntity<Page<Review>> getVenueReviews(
            @PathVariable Long venueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getVenueReviews(venueId, pageable));
    }
}
