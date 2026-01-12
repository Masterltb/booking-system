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

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
           "WHERE b.court.id = :courtId " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    boolean existsByCourtIdAndDateRange(@Param("courtId") Long courtId, 
                                        @Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.court.venue.owner.id = :ownerId ORDER BY b.createdAt DESC")
    Page<Booking> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    Optional<Booking> findByCheckinCode(String checkinCode);

    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND b.startTime >= :startOfDay AND b.endTime <= :endOfDay")
    List<Booking> findBookingsInDateRange(@Param("courtId") Long courtId, 
                                          @Param("startOfDay") LocalDateTime startOfDay, 
                                          @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b " +
           "WHERE b.court.venue.owner.id = :ownerId " +
           "AND b.status IN ('COMPLETED', 'CHECKED_IN') " +
           "AND b.startTime >= :startDate AND b.endTime <= :endDate")
    BigDecimal sumOwnerRevenueByDateRange(@Param("ownerId") Long ownerId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm các booking CONFIRMED đã quá giờ kết thúc.
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.endTime < :now")
    List<Booking> findExpiredConfirmedBookings(@Param("now") LocalDateTime now);

    /**
     * Tìm các booking CHECKED_IN đã quá giờ kết thúc.
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'CHECKED_IN' AND b.endTime < :now")
    List<Booking> findExpiredCheckedInBookings(@Param("now") LocalDateTime now);
}
