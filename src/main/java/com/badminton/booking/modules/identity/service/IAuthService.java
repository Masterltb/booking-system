package com.badminton.booking.modules.identity.service;

import com.badminton.booking.modules.identity.dto.AuthRequest;
import com.badminton.booking.modules.identity.dto.AuthResponse;
import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.dto.UserResponse;

public interface IAuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}
