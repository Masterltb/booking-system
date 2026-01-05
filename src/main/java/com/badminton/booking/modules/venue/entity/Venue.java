package com.badminton.booking.modules.venue.entity;

import com.badminton.booking.common.entity.BaseEntity;
import com.badminton.booking.common.enums.VenueStatus;
import com.badminton.booking.modules.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Venue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    @JdbcTypeCode(Types.VARCHAR) // BẮT BUỘC Hibernate map thành VARCHAR
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // SỬA LỖI: Ép kiểu VARCHAR(255)
    @Column(nullable = false)
    @JdbcTypeCode(Types.VARCHAR) // BẮT BUỘC Hibernate map thành VARCHAR
    private String address;

    private String province;
    private String district;
    private String ward;

    private Double latitude;
    private Double longitude;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "phone_contact")
    private String phoneContact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueStatus status;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Court> courts = new ArrayList<>();

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VenueImage> images = new ArrayList<>();
}
