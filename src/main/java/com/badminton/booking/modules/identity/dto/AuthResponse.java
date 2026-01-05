package com.badminton.booking.modules.identity.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UserResponse user
) {}
