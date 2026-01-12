package com.badminton.booking.modules.identity.service;

import com.badminton.booking.modules.identity.dto.WalletResponse;
import com.badminton.booking.modules.identity.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IWalletService {

    WalletResponse getMyWallet();
    void deposit(BigDecimal amount);
    void payBooking(Long bookingId);
    Page<Transaction> getMyTransactions(Pageable pageable);

    /**
     * Yêu cầu rút tiền (Dành cho Owner).
     * @param amount Số tiền muốn rút.
     * @param bankInfo Thông tin ngân hàng (STK, Tên NH, Tên CTK).
     */
    void requestWithdraw(BigDecimal amount, String bankInfo);

    /**
     * Duyệt yêu cầu rút tiền (Dành cho Admin).
     * @param transactionId ID giao dịch rút tiền.
     */
    void approveWithdraw(Long transactionId);
}
