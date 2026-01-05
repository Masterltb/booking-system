package com.badminton.booking.modules.identity.repository;

import com.badminton.booking.modules.identity.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Tìm ví của user.
     * Quan hệ 1-1 nên query đơn giản.
     */
    Optional<Wallet> findByUserId(Long userId);
}
