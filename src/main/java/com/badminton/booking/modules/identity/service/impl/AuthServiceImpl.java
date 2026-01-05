package com.badminton.booking.modules.identity.service.impl;

import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
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
import java.time.temporal.ChronoUnit; // Import thêm class này

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional // Đảm bảo User và Wallet được tạo cùng lúc (Atomic)
    public UserResponse register(RegisterRequest request) {
        // 1. Kiểm tra email trùng lặp
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email này đã được sử dụng: " + request.email());
        }

        // 2. Map DTO sang Entity
        User user = userMapper.toEntity(request);
        
        // 3. Mã hóa mật khẩu
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // 4. Lưu User trước để có ID
        User savedUser = userRepository.save(user);

        // 5. Tạo ví (Wallet) mặc định cho User
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        walletRepository.save(wallet);

        // 6. Trả về thông tin User (ẩn mật khẩu)
        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // 1. Xác thực qua AuthenticationManager
        // Nếu sai email/pass -> Ném BadCredentialsException (GlobalHandler sẽ bắt)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Load thông tin UserDetails để sinh Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        // 3. Sinh Access Token và Refresh Token
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshTokenStr = jwtUtils.generateRefreshToken(userDetails);

        // 4. Lưu Refresh Token vào DB (Xóa cái cũ nếu có)
        saveRefreshToken(user, refreshTokenStr);

        // 5. Trả về Response
        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                userMapper.toResponse(user)
        );
    }

    /**
     * Helper method: Lưu Refresh Token vào DB.
     * Nếu user đã có token cũ -> Xóa đi tạo cái mới (Single Session Policy).
     */
    private void saveRefreshToken(User user, String token) {
        // Xóa token cũ nếu tồn tại (để tránh rác DB)
        refreshTokenRepository.deleteByUser(user);

        // SỬA LỖI: Dùng ChronoUnit.MILLIS thay vì plusMillis()
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .build();
        
        refreshTokenRepository.save(refreshToken);
    }
}
