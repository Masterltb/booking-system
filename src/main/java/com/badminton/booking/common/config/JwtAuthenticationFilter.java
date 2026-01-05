package com.badminton.booking.common.config;

import com.badminton.booking.common.utils.JwtUtils;
import com.badminton.booking.modules.identity.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter này sẽ chặn mọi request gửi đến API để kiểm tra JWT Token.
 * Kế thừa OncePerRequestFilter để đảm bảo chỉ chạy 1 lần duy nhất cho mỗi request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy Header Authorization từ request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Kiểm tra xem Header có hợp lệ không (phải bắt đầu bằng "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Nếu không có token, cho request đi tiếp (để các Filter phía sau xử lý, ví dụ chặn lại nếu API yêu cầu login)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cắt bỏ chữ "Bearer " để lấy chuỗi Token gốc
        jwt = authHeader.substring(7);

        // 4. Trích xuất email từ Token
        // (Nếu token rác hoặc hết hạn, hàm này sẽ ném Exception, Spring sẽ trả về 403)
        userEmail = jwtUtils.extractUsername(jwt);

        // 5. Nếu có email và User chưa được xác thực trong Context hiện tại
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. Load thông tin User từ Database lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. Kiểm tra tính hợp lệ của Token (Signature đúng? Hết hạn chưa? Email khớp không?)
            if (jwtUtils.validateToken(jwt, userDetails)) {
                
                // 8. Tạo đối tượng Authentication chuẩn của Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Không cần credentials (password) vì đã xác thực bằng token rồi
                        userDetails.getAuthorities()
                );

                // 9. Gắn thêm thông tin request (IP, Session ID...) vào authToken
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 10. QUAN TRỌNG: Lưu authToken vào SecurityContext
                // Từ giờ trở đi, Spring Security coi như User này đã đăng nhập thành công
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Cho phép request đi tiếp đến các Filter tiếp theo hoặc Controller
        filterChain.doFilter(request, response);
    }
}
