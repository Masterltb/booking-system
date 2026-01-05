package com.badminton.booking.modules.identity.controller;

import com.badminton.booking.modules.identity.dto.AuthRequest;
import com.badminton.booking.modules.identity.dto.AuthResponse;
import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.dto.UserResponse;
import com.badminton.booking.modules.identity.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final IAuthService authService;

    /**
     * API Đăng ký tài khoản mới.
     * Endpoint: POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Yêu cầu đăng ký mới từ email: {}", request.email());
        UserResponse response = authService.register(request);
        logger.info("Đăng ký thành công cho user ID: {}", response.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API Đăng nhập.
     * Endpoint: POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Yêu cầu đăng nhập từ email: {}", request.email());
        AuthResponse response = authService.login(request);
        logger.info("Đăng nhập thành công: {}", request.email());
        return ResponseEntity.ok(response);
    }
}
