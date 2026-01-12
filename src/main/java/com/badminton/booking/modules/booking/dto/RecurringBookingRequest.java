package com.badminton.booking.modules.booking.dto;

import com.badminton.booking.common.enums.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecurringBookingRequest(
    @NotNull(message = "ID sân không được để trống")
    Long courtId,

    @NotNull(message = "Giờ bắt đầu không được để trống")
    LocalTime startTime,

    @NotNull(message = "Giờ kết thúc không được để trống")
    LocalTime endTime,

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải ở tương lai")
    LocalDate startDate,

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải ở tương lai")
    LocalDate endDate,

    @NotNull(message = "Danh sách thứ trong tuần không được để trống")
    List<String> daysOfWeek, // ["MONDAY", "WEDNESDAY", ...]

    @NotNull(message = "Phương thức thanh toán không được để trống")
    PaymentMethod paymentMethod
) {}
