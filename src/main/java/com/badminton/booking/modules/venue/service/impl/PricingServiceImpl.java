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
        // 1. Validate Giờ Chẵn (Có xử lý đặc biệt cho 23:59:59)
        boolean isStartEven = startTime.getMinute() == 0;
        boolean isEndEven = endTime.getMinute() == 0;
        
        // Nếu endTime là 23:59:59 (hoặc 23:59), coi như là chẵn (kết thúc ngày)
        if (endTime.toLocalTime().getHour() == 23 && endTime.toLocalTime().getMinute() == 59) {
            isEndEven = true;
        }

        if (!isStartEven || !isEndEven) {
            throw new AppException("Hệ thống chỉ chấp nhận đặt theo giờ chẵn (Ví dụ: 17:00, 18:00).");
        }

        // 2. Lấy thông tin sân
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", courtId));

        // 3. Lấy danh sách bảng giá
        List<PricingRule> rules = pricingRuleRepository.findByVenueIdOrderByPriorityDesc(court.getVenue().getId());

        // 4. Tính tổng số giờ
        // Làm tròn lên: 59 phút -> 1 giờ
        long totalMinutes = Duration.between(startTime, endTime).toMinutes();
        long totalHours = (long) Math.ceil(totalMinutes / 60.0);

        if (totalHours <= 0) {
            throw new AppException("Thời gian đặt tối thiểu là 1 giờ.");
        }

        // 5. Tính tiền
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime current = startTime;

        for (int i = 0; i < totalHours; i++) {
            BigDecimal hourlyPrice = findApplicablePrice(rules, current, courtId);
            totalPrice = totalPrice.add(hourlyPrice);
            
            current = current.plusHours(1);
        }

        return totalPrice;
    }

    private BigDecimal findApplicablePrice(List<PricingRule> rules, LocalDateTime currentDateTime, Long courtId) {
        BigDecimal defaultPrice = BigDecimal.valueOf(50000); 
        LocalTime time = currentDateTime.toLocalTime();
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();

        for (PricingRule rule : rules) {
            boolean isCourtMatch = (rule.getCourt() == null) || (rule.getCourt().getId().equals(courtId));
            boolean isTimeMatch = !time.isBefore(rule.getStartTime()) && time.isBefore(rule.getEndTime());
            boolean isDayMatch = checkDayOfWeek(rule.getDaysOfWeek(), dayOfWeek);

            if (isCourtMatch && isTimeMatch && isDayMatch) {
                return rule.getPricePerHour();
            }
        }
        return defaultPrice;
    }

    private boolean checkDayOfWeek(String ruleDays, DayOfWeek currentDay) {
        if (ruleDays == null || ruleDays.equalsIgnoreCase("ALL")) {
            return true;
        }
        return ruleDays.toUpperCase().contains(currentDay.name());
    }
}
