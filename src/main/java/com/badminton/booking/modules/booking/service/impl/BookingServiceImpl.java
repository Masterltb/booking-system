package com.badminton.booking.modules.booking.service.impl;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.PaymentMethod;
import com.badminton.booking.common.enums.TransactionStatus;
import com.badminton.booking.common.enums.TransactionType;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.common.service.EmailService;
import com.badminton.booking.modules.booking.dto.BookingRequest;
import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.dto.CheckinRequest;
import com.badminton.booking.modules.booking.dto.RecurringBookingRequest;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.mapper.BookingMapper;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.booking.service.IBookingService;
import com.badminton.booking.modules.identity.entity.Transaction;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.entity.Wallet;
import com.badminton.booking.modules.identity.repository.TransactionRepository;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.identity.repository.WalletRepository;
import com.badminton.booking.modules.venue.entity.Court;
import com.badminton.booking.modules.venue.repository.CourtRepository;
import com.badminton.booking.modules.venue.service.IPricingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IPricingService pricingService;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Court court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", request.courtId()));

        // 1. Validate Thời gian (Giờ bắt đầu < Giờ kết thúc)
        if (request.startTime().isAfter(request.endTime())) {
            throw new AppException("Giờ bắt đầu phải trước giờ kết thúc");
        }

        // 2. Validate Khung Giờ Mở Cửa (MỚI THÊM)
        LocalTime openTime = court.getVenue().getOpenTime();
        LocalTime closeTime = court.getVenue().getCloseTime();
        LocalTime reqStart = request.startTime().toLocalTime();
        LocalTime reqEnd = request.endTime().toLocalTime();

        if (reqStart.isBefore(openTime) || reqEnd.isAfter(closeTime)) {
            throw new AppException("Khung giờ đặt phải nằm trong giờ mở cửa của sân (" + openTime + " - " + closeTime + ")");
        }

        boolean isOverlap = bookingRepository.existsByCourtIdAndDateRange(
                request.courtId(), request.startTime(), request.endTime());
        if (isOverlap) {
            throw new ConflictException("Sân này đã có người đặt trong khung giờ bạn chọn.");
        }

        BigDecimal totalPrice = pricingService.calculatePrice(request.courtId(), request.startTime(), request.endTime());
        BigDecimal platformFee = totalPrice.multiply(BigDecimal.valueOf(0.05));

        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .totalPrice(totalPrice)
                .platformFee(platformFee)
                .status(BookingStatus.PENDING_PAY)
                .paymentMethod(request.paymentMethod())
                .isSettled(false)
                .build();

        if (request.paymentMethod() == PaymentMethod.VNPAY) {
            Booking savedBooking = bookingRepository.save(booking);
            String paymentUrl = "http://localhost:5173/mock-payment?bookingId=" + savedBooking.getId() + "&amount=" + totalPrice;
            savedBooking.setPaymentRef(paymentUrl);
            return bookingMapper.toResponse(savedBooking);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public List<BookingResponse> createRecurringBooking(RecurringBookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Court court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", request.courtId()));

        if (request.startDate().isAfter(request.endDate())) {
            throw new AppException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // Validate Khung Giờ Mở Cửa (MỚI THÊM)
        LocalTime openTime = court.getVenue().getOpenTime();
        LocalTime closeTime = court.getVenue().getCloseTime();
        if (request.startTime().isBefore(openTime) || request.endTime().isAfter(closeTime)) {
            throw new AppException("Khung giờ đặt phải nằm trong giờ mở cửa của sân (" + openTime + " - " + closeTime + ")");
        }

        List<Booking> bookingsToSave = new ArrayList<>();
        LocalDate currentDate = request.startDate();

        while (!currentDate.isAfter(request.endDate())) {
            String dayOfWeek = currentDate.getDayOfWeek().name();
            if (request.daysOfWeek().contains(dayOfWeek)) {
                
                LocalDateTime startDateTime = currentDate.atTime(request.startTime());
                LocalDateTime endDateTime = currentDate.atTime(request.endTime());

                boolean isOverlap = bookingRepository.existsByCourtIdAndDateRange(
                        request.courtId(), startDateTime, endDateTime);
                
                if (isOverlap) {
                    throw new ConflictException("Ngày " + currentDate + " đã bị trùng lịch. Vui lòng chọn khung giờ khác.");
                }

                BigDecimal totalPrice = pricingService.calculatePrice(request.courtId(), startDateTime, endDateTime);
                BigDecimal platformFee = totalPrice.multiply(BigDecimal.valueOf(0.05));

                Booking booking = Booking.builder()
                        .user(user)
                        .court(court)
                        .startTime(startDateTime)
                        .endTime(endDateTime)
                        .totalPrice(totalPrice)
                        .platformFee(platformFee)
                        .status(BookingStatus.PENDING_PAY)
                        .paymentMethod(request.paymentMethod())
                        .isSettled(false)
                        .build();
                
                bookingsToSave.add(booking);
            }
            currentDate = currentDate.plusDays(1);
        }

        if (bookingsToSave.isEmpty()) {
            throw new AppException("Không tìm thấy ngày nào phù hợp trong khoảng thời gian đã chọn.");
        }

        List<Booking> savedBookings = bookingRepository.saveAll(bookingsToSave);
        
        return savedBookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = booking.getCourt().getVenue().getOwner().getEmail().equals(email);
        boolean isUser = booking.getUser().getEmail().equals(email);

        if (!isOwner && !isUser) {
            throw new AppException("Bạn không có quyền xem booking này");
        }

        return bookingMapper.toResponse(booking);
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
        Booking booking = bookingRepository.findByCheckinCode(request.checkinCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với mã này"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException("Booking này không ở trạng thái CONFIRMED");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email).orElseThrow();
        
        if (!booking.getCourt().getVenue().getOwner().getId().equals(owner.getId())) {
            throw new AppException("Bạn không phải chủ sân này, không thể check-in.");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        
        if (!booking.getIsSettled()) {
            BigDecimal ownerReceive = booking.getTotalPrice().subtract(booking.getPlatformFee());
            
            Wallet ownerWallet = walletRepository.findByUserId(owner.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for owner"));
            
            ownerWallet.setBalance(ownerWallet.getBalance().add(ownerReceive));
            walletRepository.save(ownerWallet);
            
            Transaction transaction = Transaction.builder()
                    .wallet(ownerWallet)
                    .amount(ownerReceive)
                    .type(TransactionType.SETTLEMENT)
                    .status(TransactionStatus.SUCCESS)
                    .description("Nhận tiền booking #" + booking.getId() + " (Đã trừ phí sàn)")
                    .refId(booking.getId())
                    .build();
            transactionRepository.save(transaction);
            
            booking.setIsSettled(true);
        }

        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new AppException("Không thể hủy booking đã hoàn thành.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
