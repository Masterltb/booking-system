package com.badminton.booking.modules.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record VenueRequest(
    @NotBlank(message = "Tên sân không được để trống")
    String name,

    String description,

    @NotBlank(message = "Địa chỉ không được để trống")
    String address,

    String province,
    String district,
    String ward,

    Double latitude,
    Double longitude,

    @NotNull(message = "Giờ mở cửa không được để trống")
    LocalTime openTime,

    @NotNull(message = "Giờ đóng cửa không được để trống")
    LocalTime closeTime,

    @NotBlank(message = "Số điện thoại liên hệ không được để trống")
    String phoneContact
) {}
