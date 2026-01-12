package com.badminton.booking.modules.booking.service;

import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.dto.RecurringBookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBookingService {
    
    BookingResponse createBooking(BookingRequest request);
    
    /**
     * Tạo booking cố định (Recurring).
     * @return Danh sách các booking đã tạo.
     */
    List<BookingResponse> createRecurringBooking(RecurringBookingRequest request);

    BookingResponse getBookingById(Long id);
    Page<BookingResponse> getMyBookings(Pageable pageable);
    Page<BookingResponse> getOwnerBookings(Pageable pageable);
    void checkIn(CheckinRequest request);
    void cancelBooking(Long bookingId);
}
