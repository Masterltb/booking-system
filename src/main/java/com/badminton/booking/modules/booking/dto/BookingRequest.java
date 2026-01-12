package com.badminton.booking.modules.booking.dto;

import com.badminton.booking.common.enums.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BookingRequest(
    @NotNull(message = "ID sân không được để trống")
    Long courtId,

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @Future(message = "Giờ bắt đầu phải ở tương lai")
    LocalDateTime startTime,

    @NotNull(message = "Giờ kết thúc không được để trống")
    @Future(message = "Giờ kết thúc phải ở tương lai")
    LocalDateTime endTime,

    @NotNull(message = "Phương thức thanh toán không được để trống")
    PaymentMethod paymentMethod,

    String voucherCode // MỚI THÊM
) {}
