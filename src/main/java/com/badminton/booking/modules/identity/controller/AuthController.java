package com.badminton.booking.modules.identity.controller;

import com.badminton.booking.modules.identity.dto.AuthRequest;
import com.badminton.booking.modules.identity.dto.AuthResponse;
import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Yêu cầu đăng ký mới từ email: {}", request.email());
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Vui lòng kiểm tra email để lấy mã OTP xác thực."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        authService.verifyOtp(email, otp);
        return ResponseEntity.ok(Map.of("message", "Xác thực thành công. Bạn có thể đăng nhập ngay bây giờ."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.resendOtp(email);
        return ResponseEntity.ok(Map.of("message", "Mã OTP mới đã được gửi."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Yêu cầu đăng nhập từ email: {}", request.email());
        AuthResponse response = authService.login(request);
        logger.info("Đăng nhập thành công: {}", request.email());
        return ResponseEntity.ok(response);
    }
}
