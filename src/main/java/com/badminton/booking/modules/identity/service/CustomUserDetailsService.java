package com.badminton.booking.modules.identity.service;

import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service này dùng để kết nối Spring Security với Database User của chúng ta.
 * Nó được gọi mỗi khi có request đăng nhập hoặc xác thực token.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user trong DB theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        // 2. Chuyển đổi Role của hệ thống (ENUM) sang Authority của Spring Security
        // Ví dụ: Role.ADMIN -> SimpleGrantedAuthority("ROLE_ADMIN")
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // 3. Trả về đối tượng UserDetails chuẩn của Spring
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash()) // Password đã mã hóa
                .authorities(Collections.singletonList(authority))
                .build();
    }
}
