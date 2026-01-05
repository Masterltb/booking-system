package com.badminton.booking.modules.venue.service.impl;

import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.venue.entity.Court;
import com.badminton.booking.modules.venue.entity.PricingRule;
import com.badminton.booking.modules.venue.repository.CourtRepository;
import com.badminton.booking.modules.venue.repository.PricingRuleRepository;
import com.badminton.booking.modules.venue.service.IPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements IPricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final CourtRepository courtRepository;

    @Override
    public BigDecimal calculatePrice(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. Validate Giờ Chẵn
        if (startTime.getMinute() != 0 || endTime.getMinute() != 0) {
            throw new AppException("Hệ thống chỉ chấp nhận đặt theo giờ chẵn (Ví dụ: 17:00, 18:00).");
        }

        // 2. Lấy thông tin sân
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", courtId));

        // 3. Lấy danh sách bảng giá
        List<PricingRule> rules = pricingRuleRepository.findByVenueIdOrderByPriorityDesc(court.getVenue().getId());

        // 4. Tính tổng số giờ
        long totalHours = Duration.between(startTime, endTime).toHours();
        if (totalHours <= 0) {
            throw new AppException("Thời gian đặt tối thiểu là 1 giờ.");
        }

        // 5. Tính tiền (Vòng lặp từng giờ)
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime current = startTime;

        for (int i = 0; i < totalHours; i++) {
            // Truyền thêm ngày hiện tại (current) để check thứ
            BigDecimal hourlyPrice = findApplicablePrice(rules, current, courtId);
            totalPrice = totalPrice.add(hourlyPrice);
            
            current = current.plusHours(1);
        }

        return totalPrice;
    }

    /**
     * Tìm giá áp dụng cho một thời điểm cụ thể (Check cả Giờ và Thứ).
     */
    private BigDecimal findApplicablePrice(List<PricingRule> rules, LocalDateTime currentDateTime, Long courtId) {
        BigDecimal defaultPrice = BigDecimal.valueOf(50000); 
        LocalTime time = currentDateTime.toLocalTime();
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek(); // Lấy thứ (MONDAY, TUESDAY...)

        for (PricingRule rule : rules) {
            // Check 1: Rule có áp dụng cho Court này không?
            boolean isCourtMatch = (rule.getCourt() == null) || (rule.getCourt().getId().equals(courtId));
            
            // Check 2: Thời gian có nằm trong khung giờ của Rule không?
            boolean isTimeMatch = !time.isBefore(rule.getStartTime()) && time.isBefore(rule.getEndTime());

            // Check 3: Thứ có khớp không? (MỚI THÊM)
            // daysOfWeek lưu dạng chuỗi: "MONDAY,TUESDAY,WEDNESDAY" hoặc "ALL"
            boolean isDayMatch = checkDayOfWeek(rule.getDaysOfWeek(), dayOfWeek);

            if (isCourtMatch && isTimeMatch && isDayMatch) {
                return rule.getPricePerHour();
            }
        }
        return defaultPrice;
    }

    /**
     * Helper check thứ trong tuần.
     */
    private boolean checkDayOfWeek(String ruleDays, DayOfWeek currentDay) {
        if (ruleDays == null || ruleDays.equalsIgnoreCase("ALL")) {
            return true; // Áp dụng cho mọi ngày
        }
        // Kiểm tra xem chuỗi ruleDays có chứa tên thứ hiện tại không
        // Ví dụ: ruleDays="SATURDAY,SUNDAY", currentDay=SATURDAY -> true
        return ruleDays.toUpperCase().contains(currentDay.name());
    }
}
