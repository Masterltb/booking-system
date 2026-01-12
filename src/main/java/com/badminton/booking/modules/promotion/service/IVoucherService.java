package com.badminton.booking.modules.promotion.service;

import com.badminton.booking.modules.promotion.dto.VoucherRequest;


import java.math.BigDecimal;

public interface IVoucherService {
    
    void createVoucher(VoucherRequest request);
    
    /**
     * Tính toán số tiền giảm giá.
     * @param code Mã voucher.
     * @param originalPrice Giá gốc đơn hàng.
     * @param venueId ID sân (để check scope).
     * @return Số tiền được giảm.
     */
    BigDecimal calculateDiscount(String code, BigDecimal originalPrice, Long venueId);

    /**
     * Đánh dấu voucher đã được sử dụng (Tăng usedCount).
     */
    void useVoucher(String code);
}
