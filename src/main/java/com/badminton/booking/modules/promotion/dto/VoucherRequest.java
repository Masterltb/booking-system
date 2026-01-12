package com.badminton.booking.modules.promotion.dto;

import com.badminton.booking.common.enums.DiscountType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherRequest(
    @NotBlank(message = "Mã voucher không được để trống")
    String code,

    @NotNull(message = "Loại giảm giá không được để trống")
    DiscountType discountType,

    @NotNull(message = "Giá trị giảm không được để trống")
    BigDecimal discountValue,

    BigDecimal minOrderValue,
    BigDecimal maxDiscount,
    Integer usageLimit,

    @NotNull(message = "Ngày bắt đầu không được để trống")
    LocalDateTime startDate,

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải ở tương lai")
    LocalDateTime endDate,

    Long venueId // Nullable
) {}
