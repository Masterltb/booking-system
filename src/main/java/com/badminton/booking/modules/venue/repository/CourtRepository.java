package com.badminton.booking.modules.venue.repository;

import com.badminton.booking.modules.venue.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    /**
     * Lấy tất cả sân con của một cụm sân.
     * Dùng cho API check availability.
     */
    List<Court> findByVenueId(Long venueId);
}
