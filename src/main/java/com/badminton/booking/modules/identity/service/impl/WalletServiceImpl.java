package com.badminton.booking.modules.identity.service.impl;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.TransactionStatus;
import com.badminton.booking.common.enums.TransactionType;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.common.service.EmailService;
import com.badminton.booking.modules.booking.entity.Booking;
import com.badminton.booking.modules.booking.repository.BookingRepository;
import com.badminton.booking.modules.identity.dto.WalletResponse;
import com.badminton.booking.modules.identity.entity.Transaction;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.entity.Wallet;
import com.badminton.booking.modules.identity.repository.TransactionRepository;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.identity.repository.WalletRepository;
import com.badminton.booking.modules.identity.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Override
    public WalletResponse getMyWallet() {
        User user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        
        return new WalletResponse(wallet.getId(), wallet.getBalance(), wallet.getCurrency());
    }

    @Override
    @Transactional
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Số tiền nạp phải lớn hơn 0");
        }

        User user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description("Nạp tiền vào ví")
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void payBooking(Long bookingId) {
        User user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAY) {
            throw new AppException("Booking này không ở trạng thái chờ thanh toán");
        }
        if (wallet.getBalance().compareTo(booking.getTotalPrice()) < 0) {
            throw new AppException("Số dư ví không đủ để thanh toán");
        }

        wallet.setBalance(wallet.getBalance().subtract(booking.getTotalPrice()));
        walletRepository.save(wallet);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckinCode(RandomStringUtils.randomAlphanumeric(6).toUpperCase());
        bookingRepository.save(booking);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(booking.getTotalPrice().negate())
                .type(TransactionType.BOOKING_PAYMENT)
                .status(TransactionStatus.SUCCESS)
                .description("Thanh toán booking #" + bookingId)
                .refId(bookingId)
                .build();
        transactionRepository.save(transaction);

        emailService.sendBookingConfirmation(booking);
    }

    @Override
    public Page<Transaction> getMyTransactions(Pageable pageable) {
        User user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    @Override
    @Transactional
    public void requestWithdraw(BigDecimal amount, String bankInfo) {
        User user = getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // 1. Validate số dư
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Số tiền rút phải lớn hơn 0");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException("Số dư không đủ để rút");
        }

        // 2. Trừ tiền ngay lập tức (Pending)
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 3. Tạo Transaction PENDING
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount.negate()) // Số âm
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.PENDING) // Chờ Admin duyệt
                .description("Yêu cầu rút tiền về: " + bankInfo)
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void approveWithdraw(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getType() != TransactionType.WITHDRAW) {
            throw new AppException("Đây không phải giao dịch rút tiền");
        }
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new AppException("Giao dịch này đã được xử lý rồi");
        }

        // Update trạng thái thành công
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
        
        // TODO: Gửi email thông báo cho Owner là tiền đã được chuyển
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
