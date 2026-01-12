package com.badminton.booking.common.enums;

public enum BookingStatus {
    PENDING_PAY, // Vừa tạo, chưa thanh toán
    CONFIRMED,   // Đã thanh toán thành công
    CANCELLED,   // Đã hủy (do user hoặc timeout)
    COMPLETED,   // Đã hoàn thành (đã đá xong hoặc hết giờ)
    CHECKED_IN,  // Khách đã đến sân check-in
    BLOCKED      // Sân bị khóa bởi chủ sân (Bảo trì/Giải đấu) - MỚI
}
