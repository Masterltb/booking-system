package com.badminton.booking.modules.identity.service;

import com.badminton.booking.modules.identity.dto.AuthRequest;
import com.badminton.booking.modules.identity.dto.AuthResponse;
import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.dto.UserResponse;

public interface IAuthService {
    
    /**
     * Đăng ký tài khoản mới (Gửi OTP).
     */
    void register(RegisterRequest request);

    /**
     * Xác thực OTP để kích hoạt tài khoản.
     */
    void verifyOtp(String email, String otp);

    /**
     * Gửi lại mã OTP.
     */
    void resendOtp(String email);

    /**
     * Đăng nhập.
     */
    AuthResponse login(AuthRequest request);
}
