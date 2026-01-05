package com.badminton.booking.modules.booking.service;

import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBookingService {
    
    /**
     * Tạo booking mới.
     */
    BookingResponse createBooking(BookingRequest request);

    /**
     * Lấy danh sách booking của User đang đăng nhập.
     */
    Page<BookingResponse> getMyBookings(Pageable pageable);

    /**
     * Lấy danh sách booking của Owner (để quản lý sân).
     */
    Page<BookingResponse> getOwnerBookings(Pageable pageable);

    /**
     * Check-in cho khách (Dành cho Owner).
     */
    void checkIn(CheckinRequest request);
    
    /**
     * Hủy booking (User hủy hoặc System hủy do timeout).
     */
    void cancelBooking(Long bookingId);
}
