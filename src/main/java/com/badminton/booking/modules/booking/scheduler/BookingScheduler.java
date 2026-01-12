package com.badminton.booking.modules.booking.scheduler;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.booking.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BookingScheduler.class);
    private final BookingRepository bookingRepository;
    private final IBookingService bookingService;

    /**
     * Chạy mỗi 1 tiếng để quét các booking quá hạn.
     * Cron: "0 0 * * * *" (Đầu mỗi giờ)
     * Để test nhanh, bạn có thể đổi thành "0 * * * * *" (Mỗi phút)
     */
    @Scheduled(cron = "0 0 * * * *") 
    @Transactional
    public void autoCompleteBookings() {
        logger.info("--- Bắt đầu Cron Job: Auto Complete Bookings ---");
        LocalDateTime now = LocalDateTime.now();

        // 1. Xử lý CONFIRMED quá hạn (Quên check-in -> Tự động settlement)
        List<Booking> expiredConfirmed = bookingRepository.findExpiredConfirmedBookings(now);
        for (Booking booking : expiredConfirmed) {
            try {
                logger.info("Auto settling & completing booking #{}", booking.getId());
                bookingService.settleBooking(booking); // Cộng tiền
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);
            } catch (Exception e) {
                logger.error("Error processing booking #{}: {}", booking.getId(), e.getMessage());
            }
        }

        // 2. Xử lý CHECKED_IN quá hạn (Đã settlement rồi -> Chỉ update status)
        List<Booking> expiredCheckedIn = bookingRepository.findExpiredCheckedInBookings(now);
        for (Booking booking : expiredCheckedIn) {
            try {
                logger.info("Auto completing checked-in booking #{}", booking.getId());
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);
            } catch (Exception e) {
                logger.error("Error processing booking #{}: {}", booking.getId(), e.getMessage());
            }
        }
        
        logger.info("--- Kết thúc Cron Job ---");
    }
}
