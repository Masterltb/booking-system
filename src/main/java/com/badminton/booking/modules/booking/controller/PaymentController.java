package com.badminton.booking.modules.booking.controller;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.service.EmailService;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.identity.repository.TransactionRepository;
import com.badminton.booking.modules.identity.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService; // Inject EmailService

    /**
     * API Giả lập IPN (Instant Payment Notification) từ VNPay.
     * Khi user thanh toán xong, VNPay (hoặc trang giả lập) sẽ gọi API này.
     * 
     * @param responseCode "00" là thành công, khác là thất bại.
     * @param bookingId ID của booking.
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> processIpn(
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") Long bookingId
    ) {
        Map<String, String> response = new HashMap<>();
        
        // 1. Tìm Booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElse(null);

        if (booking == null) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return ResponseEntity.ok(response);
        }

        // 2. Kiểm tra trạng thái đơn hàng (để tránh xử lý trùng lặp)
        if (booking.getStatus() != BookingStatus.PENDING_PAY) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return ResponseEntity.ok(response);
        }

        // 3. Xử lý kết quả
        if ("00".equals(responseCode)) {
            // THANH TOÁN THÀNH CÔNG
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCheckinCode(generateCheckinCode());
            bookingRepository.save(booking);
            
            // Gửi Email (Async)
            emailService.sendBookingConfirmation(booking);
            
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            // THANH TOÁN THẤT BẠI
            booking.setStatus(BookingStatus.CANCELLED); // Hoặc giữ PENDING tùy policy
            bookingRepository.save(booking);
            
            response.put("RspCode", "00");
            response.put("Message", "Cancel Success");
        }

        return ResponseEntity.ok(response);
    }

    private String generateCheckinCode() {
        return org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }
}
