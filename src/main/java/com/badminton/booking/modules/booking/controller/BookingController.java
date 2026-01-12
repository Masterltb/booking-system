package com.badminton.booking.modules.booking.controller;

import com.badminton.booking.modules.booking.dto.BlockSlotRequest;
import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.dto.RecurringBookingRequest;
import com.badminton.booking.modules.booking.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final IBookingService bookingService;

    // --- PUBLIC API ---

    /**
     * Xem lịch trống của sân (Availability).
     * Endpoint: GET /api/v1/public/venues/{venueId}/availability
     */
    @GetMapping("/public/venues/{venueId}/availability")
    public ResponseEntity<List<BookingResponse>> getAvailability(
            @PathVariable Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long courtId
    ) {
        return ResponseEntity.ok(bookingService.getAvailability(venueId, date, courtId));
    }

    // --- USER API ---

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        logger.info("Yêu cầu đặt sân courtId: {} từ {}", request.courtId(), request.startTime());
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/bookings/recurring")
    public ResponseEntity<List<BookingResponse>> createRecurringBooking(@Valid @RequestBody RecurringBookingRequest request) {
        logger.info("Yêu cầu đặt lịch cố định courtId: {}", request.courtId());
        List<BookingResponse> responses = bookingService.createRecurringBooking(request);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/bookings/my-bookings")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getMyBookings(pageable));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // --- PARTNER API (Owner) ---

    @PostMapping("/partner/bookings/check-in")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> checkIn(@Valid @RequestBody CheckinRequest request) {
        bookingService.checkIn(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/partner/bookings")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<BookingResponse>> getOwnerBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getOwnerBookings(pageable));
    }

    @PostMapping("/partner/bookings/block")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> blockSlot(@Valid @RequestBody BlockSlotRequest request) {
        bookingService.blockSlot(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/partner/bookings/unblock/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> unblockSlot(@PathVariable Long id) {
        bookingService.unblockSlot(id);
        return ResponseEntity.ok().build();
    }
}
