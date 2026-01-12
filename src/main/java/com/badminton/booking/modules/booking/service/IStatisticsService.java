package com.badminton.booking.modules.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface IStatisticsService {

    /**
     * Lấy thống kê doanh thu cho Owner.
     * @param fromDate Ngày bắt đầu.
     * @param toDate Ngày kết thúc.
     * @return Map chứa totalRevenue, totalBookings.
     */
    Map<String, Object> getOwnerRevenueStats(LocalDate fromDate, LocalDate toDate);
}
