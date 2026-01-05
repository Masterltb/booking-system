package com.badminton.booking.common.enums;

public enum TransactionType {
    DEPOSIT,              // Nạp tiền vào ví
    WITHDRAW,             // Rút tiền khỏi ví
    BOOKING_PAYMENT,      // Thanh toán đặt sân
    BOOKING_REFUND,       // Hoàn tiền hủy sân
    SETTLEMENT,           // Đối soát trả tiền cho chủ sân (100%)
    SUBSCRIPTION_PAYMENT  // Chủ sân thanh toán gói cước hàng tháng
}
