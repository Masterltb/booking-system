package com.badminton.booking.modules.booking.dto;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
    Long id,
    Long courtId,
    String courtName,
    String venueName,
    String venueAddress,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal totalPrice,
    BookingStatus status,
    PaymentMethod paymentMethod,
    String checkinCode, // Chỉ hiện khi status = CONFIRMED
    LocalDateTime createdAt
) {}
