package com.badminton.booking.common.config;

import com.badminton.booking.modules.identity.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Class cấu hình chính cho Spring Security 6.
 * Nơi quy định các luật lệ bảo mật, phân quyền URL và cơ chế mã hóa.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize trên từng method Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 1. Cấu hình chuỗi lọc bảo mật (Security Filter Chain).
     * Đây là nơi quan trọng nhất, quyết định request nào được qua, request nào bị chặn.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT (Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Cấu hình CORS (để Frontend gọi được API)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Phân quyền truy cập URL
            .authorizeHttpRequests(auth -> auth
                // Các API Auth và Public được phép truy cập không cần Token
                .requestMatchers("/api/v1/auth/**", "/api/v1/public/**").permitAll()
                
                // Các API Admin bắt buộc phải có Role ADMIN
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // Các API Partner bắt buộc phải có Role OWNER
                .requestMatchers("/api/v1/partner/**").hasRole("OWNER")
                
                // Tất cả các request còn lại bắt buộc phải đăng nhập
                .anyRequest().authenticated()
            )

            // Cấu hình Session là STATELESS (Không lưu trạng thái đăng nhập phía Server)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Khai báo AuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Chèn JwtFilter vào trước UsernamePasswordAuthenticationFilter
            // Để nó kiểm tra Token trước khi Spring Security xử lý đăng nhập truyền thống
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 2. Cấu hình AuthenticationProvider.
     * Bean này giúp Spring biết cách tìm User (qua UserDetailsService) và check pass (qua PasswordEncoder).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 3. Cấu hình AuthenticationManager.
     * Bean này sẽ được gọi trong AuthService để thực hiện đăng nhập thủ công.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 4. Cấu hình PasswordEncoder.
     * Sử dụng thuật toán BCrypt (chuẩn công nghiệp) để băm mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 5. Cấu hình CORS (Cross-Origin Resource Sharing).
     * Cho phép Frontend (ví dụ chạy ở localhost:3000) gọi API của Backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Tạm thời cho phép tất cả (Production nên giới hạn domain)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
