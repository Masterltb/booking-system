package com.badminton.booking.modules.venue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;

public record PricingRuleRequest(
    Long courtId, // Nullable (Nếu null -> áp dụng cho cả Venue)

    @NotNull(message = "Giờ bắt đầu không được để trống")
    LocalTime startTime,

    @NotNull(message = "Giờ kết thúc không được để trống")
    LocalTime endTime,

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền không được âm")
    BigDecimal pricePerHour,

    Integer priority, // Default 1

    String daysOfWeek // "MON,TUE" hoặc "ALL"
) {}
