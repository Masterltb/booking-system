package com.badminton.booking.modules.venue.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IPricingService {
    /**
     * Tính tổng tiền thuê sân.
     * @param courtId ID sân con.
     * @param startTime Giờ bắt đầu.
     * @param endTime Giờ kết thúc.
     * @return Tổng tiền (Chưa bao gồm phí sàn).
     */
    BigDecimal calculatePrice(Long courtId, LocalDateTime startTime, LocalDateTime endTime);
}
