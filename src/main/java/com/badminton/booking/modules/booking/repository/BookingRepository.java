package com.badminton.booking.modules.booking.repository;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.modules.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * CORE LOGIC: Kiểm tra trùng lịch (Overlap).
     * Một booking bị coi là trùng nếu:
     * 1. Cùng sân (court_id).
     * 2. Trạng thái KHÔNG PHẢI là CANCELLED.
     * 3. Thời gian giao nhau: (StartA < EndB) AND (EndA > StartB).
     *
     * @return true nếu có ít nhất 1 booking trùng.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM Booking b " +
           "WHERE b.court.id = :courtId " +
           "AND b.status != 'CANCELLED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsByCourtIdAndDateRange(
            @Param("courtId") Long courtId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Lấy danh sách booking trong một khoảng thời gian (VD: 1 ngày) của 1 sân.
     * Dùng cho API Get Availability (3.1).
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.court.id = :courtId " +
           "AND b.status != 'CANCELLED' " +
           "AND b.startTime >= :fromTime " +
           "AND b.endTime <= :toTime")
    List<Booking> findBookingsInDateRange(
            @Param("courtId") Long courtId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    /**
     * Tìm booking theo mã check-in (Dùng cho chủ sân check-in khách).
     */
    Optional<Booking> findByCheckinCode(String checkinCode);

    /**
     * Lấy lịch sử đặt sân của User.
     */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Lấy danh sách booking thuộc về các sân của một Chủ Sân (Owner).
     * Dùng để Owner quản lý lịch đặt.
     */
    @Query("SELECT b FROM Booking b WHERE b.court.venue.owner.id = :ownerId ORDER BY b.startTime DESC")
    Page<Booking> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * Tìm các booking cần đối soát (Settlement Job).
     * Điều kiện:
     * 1. Status là COMPLETED hoặc CHECKED_IN.
     * 2. Chưa đối soát (isSettled = false).
     */
    @Query("SELECT b FROM Booking b WHERE b.isSettled = false AND " +
           "(b.status = 'COMPLETED' OR b.status = 'CHECKED_IN')")
    List<Booking> findReadyForSettlement();

    // --- ANALYTICS QUERIES (MỚI) ---

    /**
     * Tính tổng doanh thu hoa hồng (Platform Fee) trong khoảng thời gian.
     * Dùng cho Admin Dashboard.
     */
    @Query("SELECT SUM(b.platformFee) FROM Booking b " +
           "WHERE b.status = 'COMPLETED' " +
           "AND b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumPlatformFeeByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng doanh thu thực nhận của Chủ Sân (Sau khi trừ phí).
     * Dùng cho Owner Dashboard.
     */
    @Query("SELECT SUM(b.totalPrice - b.platformFee) FROM Booking b " +
           "WHERE b.court.venue.owner.id = :ownerId " +
           "AND b.status = 'COMPLETED' " +
           "AND b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumOwnerRevenueByDateRange(@Param("ownerId") Long ownerId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
