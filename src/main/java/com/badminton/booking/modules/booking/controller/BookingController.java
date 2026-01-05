package com.badminton.booking.modules.booking.controller;


import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final IBookingService bookingService;

    // --- USER API ---

    /**
     * Đặt sân mới.
     * Endpoint: POST /api/v1/bookings
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        logger.info("Yêu cầu đặt sân courtId: {} từ {}", request.courtId(), request.startTime());
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Xem lịch sử đặt sân của tôi.
     * Endpoint: GET /api/v1/bookings/my-bookings
     */
    @GetMapping("/bookings/my-bookings")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getMyBookings(pageable));
    }

    /**
     * Hủy đặt sân.
     * Endpoint: POST /api/v1/bookings/{id}/cancel
     */
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // --- PARTNER API (Owner) ---

    /**
     * Check-in cho khách.
     * Endpoint: POST /api/v1/partner/bookings/check-in
     */
    @PostMapping("/partner/bookings/check-in")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> checkIn(@Valid @RequestBody CheckinRequest request) {
        bookingService.checkIn(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Xem danh sách booking của các sân mình quản lý.
     * Endpoint: GET /api/v1/partner/bookings
     */
    @GetMapping("/partner/bookings")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<BookingResponse>> getOwnerBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getOwnerBookings(pageable));
    }
}
