package com.badminton.booking.modules.booking.entity;

import com.badminton.booking.common.entity.BaseEntity;
import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.PaymentMethod;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.venue.entity.Court;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_time", columnList = "start_time, end_time"),
        @Index(name = "idx_booking_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(name = "checkin_code", length = 6, unique = true)
    private String checkinCode;

    @Column(name = "is_settled")
    @Builder.Default
    private Boolean isSettled = false;
}
