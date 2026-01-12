package com.badminton.booking.modules.promotion.repository;

import com.badminton.booking.modules.promotion.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    /**
     * Tìm voucher khả dụng.
     * Điều kiện: Code đúng, Active, Còn hạn, Venue khớp (hoặc Global).
     */
    @Query("SELECT v FROM Voucher v WHERE v.code = :code " +
           "AND v.isActive = true " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "AND (v.venue IS NULL OR v.venue.id = :venueId)")
    Optional<Voucher> findValidVoucher(@Param("code") String code, 
                                       @Param("venueId") Long venueId, 
                                       @Param("now") LocalDateTime now);
}
