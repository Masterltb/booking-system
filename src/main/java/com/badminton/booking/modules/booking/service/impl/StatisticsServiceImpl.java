package com.badminton.booking.modules.booking.service.impl;

import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.booking.service.IStatisticsService;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public Map<String, Object> getOwnerRevenueStats(LocalDate fromDate, LocalDate toDate) {
        // 1. Lấy Owner hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Convert LocalDate -> LocalDateTime (Start of day, End of day)
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        // 3. Gọi Repository tính tổng doanh thu
        BigDecimal totalRevenue = bookingRepository.sumOwnerRevenueByDateRange(owner.getId(), start, end);

        // 4. Đếm số lượng booking (Tạm thời chưa có hàm count trong Repo, ta có thể thêm sau hoặc dùng logic khác)
        // Ở đây MVP trả về doanh thu trước
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        result.put("period", fromDate + " to " + toDate);
        
        return result;
    }
}
