package com.badminton.booking.modules.promotion.service.impl;

import com.badminton.booking.common.enums.DiscountType;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.promotion.dto.VoucherRequest;
import com.badminton.booking.modules.promotion.entity.Voucher;
import com.badminton.booking.modules.promotion.repository.VoucherRepository;
import com.badminton.booking.modules.promotion.service.IVoucherService;
import com.badminton.booking.modules.venue.entity.Venue;
import com.badminton.booking.modules.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createVoucher(VoucherRequest request) {
        // 1. Check trùng code
        if (voucherRepository.findByCode(request.code()).isPresent()) {
            throw new ConflictException("Mã voucher đã tồn tại");
        }

        // 2. Check quyền Owner (nếu tạo cho Venue)
        Venue venue = null;
        if (request.venueId() != null) {
            venue = venueRepository.findById(request.venueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.venueId()));
            
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User owner = userRepository.findByEmail(email).orElseThrow();
            
            if (!venue.getOwner().getId().equals(owner.getId())) {
                throw new AppException("Bạn không có quyền tạo voucher cho sân này");
            }
        }

        // 3. Lưu Voucher
        Voucher voucher = Voucher.builder()
                .code(request.code().toUpperCase())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderValue(request.minOrderValue() != null ? request.minOrderValue() : BigDecimal.ZERO)
                .maxDiscount(request.maxDiscount())
                .usageLimit(request.usageLimit())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .venue(venue)
                .build();
        
        voucherRepository.save(voucher);
    }

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal originalPrice, Long venueId) {
        Voucher voucher = voucherRepository.findValidVoucher(code, venueId, LocalDateTime.now())
                .orElseThrow(() -> new AppException("Voucher không hợp lệ hoặc đã hết hạn"));

        // Check Min Order Value
        if (originalPrice.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher này");
        }

        // Check Usage Limit
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException("Voucher đã hết lượt sử dụng");
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else {
            // Percentage
            discount = originalPrice.multiply(voucher.getDiscountValue().divide(BigDecimal.valueOf(100)));
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        }

        // Không được giảm quá giá trị đơn hàng
        if (discount.compareTo(originalPrice) > 0) {
            return originalPrice;
        }

        return discount;
    }

    @Override
    @Transactional
    public void useVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code).orElseThrow();
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }
}
