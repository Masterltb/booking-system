package com.badminton.booking.modules.venue.repository;

import com.badminton.booking.modules.venue.entity.VenueImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueImageRepository extends JpaRepository<VenueImage, Long> {

    /**
     * Lấy tất cả ảnh của một Venue.
     */
    List<VenueImage> findByVenueId(Long venueId);

    /**
     * Xóa tất cả ảnh của một Venue (Dùng khi xóa sân hoặc reset ảnh).
     */
    @Modifying
    void deleteByVenueId(Long venueId);
}
