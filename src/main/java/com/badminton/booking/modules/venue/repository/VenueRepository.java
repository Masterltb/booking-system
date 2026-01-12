package com.badminton.booking.modules.venue.repository;

import com.badminton.booking.common.enums.VenueStatus;
import com.badminton.booking.modules.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * Tìm kiếm sân (API 3.1) - NÂNG CẤP.
     * Hỗ trợ tìm theo Keyword, Province, District.
     * Vẫn dùng Native Query để tránh lỗi bytea.
     */
    @Query(value = "SELECT * FROM venues v WHERE v.status = 'ACTIVE' " +
                   "AND (:keyword IS NULL OR LOWER(CAST(v.name AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(v.address AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "AND (:province IS NULL OR v.province = :province) " +
                   "AND (:district IS NULL OR v.district = :district)",
           countQuery = "SELECT count(*) FROM venues v WHERE v.status = 'ACTIVE' " +
                        "AND (:keyword IS NULL OR LOWER(CAST(v.name AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(v.address AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:province IS NULL OR v.province = :province) " +
                        "AND (:district IS NULL OR v.district = :district)",
           nativeQuery = true)
    Page<Venue> search(
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("district") String district,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"images", "courts"})
    Optional<Venue> findWithDetailsById(Long id);

    Page<Venue> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Venue> findByStatus(VenueStatus status, Pageable pageable);
}
