package com.badminton.booking.common.config;

import com.badminton.booking.common.utils.JwtUtils;
import com.badminton.booking.modules.identity.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Kiểm tra Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // 2. Trích xuất email (Có thể ném Exception nếu token hết hạn hoặc sai)
            userEmail = jwtUtils.extractUsername(jwt);

            // 3. Xác thực User
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtils.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            
            // Cho phép đi tiếp nếu mọi thứ OK
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            // Bắt lỗi Token hết hạn -> Trả về 401 JSON
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            // Bắt các lỗi Token khác (sai chữ ký, rỗng...) -> Trả về 401 JSON
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid Token");
        } catch (Exception ex) {
            // Lỗi hệ thống khác -> 500
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Authentication Error");
        }
    }

    /**
     * Helper method để trả về lỗi JSON thủ công từ Filter
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", ""); // Trong Filter khó lấy path chính xác, để trống hoặc lấy từ request.getRequestURI()

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
