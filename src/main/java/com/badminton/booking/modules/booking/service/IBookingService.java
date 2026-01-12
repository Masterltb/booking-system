package com.badminton.booking.modules.booking.service;

import com.badminton.booking.modules.booking.dto.BlockSlotRequest;
import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.dto.RecurringBookingRequest;
import com.badminton.booking.modules.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IBookingService {
    
    BookingResponse createBooking(BookingRequest request);
    List<BookingResponse> createRecurringBooking(RecurringBookingRequest request);
    BookingResponse getBookingById(Long id);
    Page<BookingResponse> getMyBookings(Pageable pageable);
    Page<BookingResponse> getOwnerBookings(Pageable pageable);
    void checkIn(CheckinRequest request);
    void cancelBooking(Long bookingId);
    void blockSlot(BlockSlotRequest request);
    void unblockSlot(Long bookingId);
    List<BookingResponse> getAvailability(Long venueId, LocalDate date, Long courtId);

    /**
     * Thực hiện đối soát tiền cho booking (Cộng tiền cho chủ sân).
     * Dùng cho Check-in và Auto Scheduler.
     */
    void settleBooking(Booking booking);
}
