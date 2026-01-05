package com.badminton.booking.modules.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckinRequest(
    @NotBlank(message = "Mã check-in không được để trống")
    @Size(min = 6, max = 6, message = "Mã check-in phải có 6 ký tự")
    String checkinCode
) {}
