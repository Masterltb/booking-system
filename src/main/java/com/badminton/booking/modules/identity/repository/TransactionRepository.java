package com.badminton.booking.modules.identity.repository;

import com.badminton.booking.modules.identity.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Lấy lịch sử giao dịch của 1 ví.
     * Luôn phân trang (Pageable) vì số lượng giao dịch có thể rất lớn.
     * Sắp xếp mặc định nên là created_at DESC (mới nhất lên đầu).
     */
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
}
