package com.badminton.booking.modules.booking.service.impl;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.mapper.BookingMapper;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.booking.service.IBookingService;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.venue.entity.Court;
import com.badminton.booking.modules.venue.repository.CourtRepository;
import com.badminton.booking.modules.venue.service.IPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final IPricingService pricingService;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Lấy User đang đăng nhập
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Validate Court
        Court court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", request.courtId()));

        // 3. Validate Thời gian (Logic Giờ Chẵn đã check ở PricingService, nhưng check lại ở đây cho chắc)
        if (request.startTime().isAfter(request.endTime())) {
            throw new AppException("Giờ bắt đầu phải trước giờ kết thúc");
        }

        // 4. Check Trùng Lịch (Critical)
        boolean isOverlap = bookingRepository.existsByCourtIdAndDateRange(
                request.courtId(), request.startTime(), request.endTime());
        if (isOverlap) {
            throw new ConflictException("Sân này đã có người đặt trong khung giờ bạn chọn.");
        }

        // 5. Tính tiền
        BigDecimal totalPrice = pricingService.calculatePrice(request.courtId(), request.startTime(), request.endTime());
        
        // 6. Tính phí sàn (5%)
        BigDecimal platformFee = totalPrice.multiply(BigDecimal.valueOf(0.05));

        // 7. Tạo Booking Entity
        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .totalPrice(totalPrice)
                .platformFee(platformFee)
                .status(BookingStatus.PENDING_PAY) // Mặc định là chờ thanh toán
                .paymentMethod(request.paymentMethod())
                .isSettled(false)
                .build();

        // 8. Lưu vào DB
        Booking savedBooking = bookingRepository.save(booking);

        // TODO: Tích hợp VNPay ở đây (Sẽ trả về Payment URL trong response)
        
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    public Page<BookingResponse> getOwnerBookings(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByOwnerId(owner.getId(), pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional
    public void checkIn(CheckinRequest request) {
        // 1. Tìm Booking theo mã check-in
        Booking booking = bookingRepository.findByCheckinCode(request.checkinCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với mã này"));

        // 2. Validate Status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException("Booking này không ở trạng thái CONFIRMED (Có thể chưa thanh toán hoặc đã hủy)");
        }

        // 3. Validate Owner (Chỉ chủ sân của sân đó mới được check-in)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getCourt().getVenue().getOwner().getEmail().equals(email)) {
            throw new AppException("Bạn không phải chủ sân này, không thể check-in.");
        }

        // 4. Update Status
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Logic hủy: Chỉ cho hủy nếu đang PENDING_PAY hoặc CONFIRMED (nhưng phải trước giờ đá X tiếng - Tùy policy)
        // Ở đây MVP cho hủy thoải mái nếu chưa COMPLETED
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new AppException("Không thể hủy booking đã hoàn thành.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // TODO: Nếu đã thanh toán (CONFIRMED) -> Trigger hoàn tiền (Refund)
    }
}
