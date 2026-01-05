package com.badminton.booking.modules.identity.dto;

import com.badminton.booking.common.enums.Role;
import com.badminton.booking.common.enums.UserStatus;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String phone,
    String avatarUrl,
    Role role,
    UserStatus status
) {}
