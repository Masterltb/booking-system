package com.badminton.booking.modules.identity.service.impl;

import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.common.service.EmailService;
import com.badminton.booking.common.utils.JwtUtils;
import com.badminton.booking.modules.identity.dto.AuthRequest;
import com.badminton.booking.modules.identity.dto.AuthResponse;
import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.dto.UserResponse;
import com.badminton.booking.modules.identity.entity.RefreshToken;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.entity.Wallet;
import com.badminton.booking.modules.identity.mapper.UserMapper;
import com.badminton.booking.modules.identity.repository.RefreshTokenRepository;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.identity.repository.WalletRepository;
import com.badminton.booking.modules.identity.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email này đã được sử dụng: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.INACTIVE);

        String otp = RandomStringUtils.randomNumeric(6);
        user.setOtpCode(otp);
        user.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);
        logger.info("Đã tạo user tạm thời: {}, OTP: {}", user.getEmail(), otp);

        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        logger.info("Đang xác thực OTP cho email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getStatus() == UserStatus.ACTIVE) {
            logger.warn("User {} đã kích hoạt trước đó.", email);
            throw new AppException("Tài khoản này đã được kích hoạt rồi.");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            logger.error("OTP không khớp cho user {}. Nhập: {}, DB: {}", email, otp, user.getOtpCode());
            throw new AppException("Mã OTP không chính xác.");
        }

        if (user.getOtpExpiryDate().isBefore(LocalDateTime.now())) {
            logger.error("OTP đã hết hạn cho user {}", email);
            throw new AppException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }

        // OTP OK
        user.setStatus(UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiryDate(null);
        userRepository.save(user);
        
        logger.info("Kích hoạt thành công user: {}", email);

        createWalletForUser(user);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AppException("Tài khoản này đã được kích hoạt rồi.");
        }

        String otp = RandomStringUtils.randomNumeric(6);
        user.setOtpCode(otp);
        user.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
        }

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshTokenStr = jwtUtils.generateRefreshToken(userDetails);

        saveRefreshToken(user, refreshTokenStr);

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                userMapper.toResponse(user)
        );
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .build();
        
        refreshTokenRepository.save(refreshToken);
    }

    private void createWalletForUser(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        walletRepository.save(wallet);
    }
}
