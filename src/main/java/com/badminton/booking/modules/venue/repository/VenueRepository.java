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
     * Tìm kiếm sân (API 3.1).
     * SỬA LỖI TRIỆT ĐỂ: Chuyển sang dùng NATIVE QUERY (SQL thuần).
     * Lý do: Để bỏ qua cơ chế dịch JPQL của Hibernate đang gây lỗi hiểu lầm kiểu dữ liệu (bytea).
     * Với Native Query, ta kiểm soát 100% câu lệnh gửi xuống DB.
     */
    @Query(value = "SELECT * FROM venues v WHERE v.status = 'ACTIVE' AND " +
                   "(:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "LOWER(v.address) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT count(*) FROM venues v WHERE v.status = 'ACTIVE' AND " +
                        "(:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(v.address) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           nativeQuery = true)
    Page<Venue> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy chi tiết sân (API 3.2).
     */
    @EntityGraph(attributePaths = {"images", "courts"})
    Optional<Venue> findWithDetailsById(Long id);

    /**
     * Lấy danh sách sân của một chủ sân (Owner).
     */
    Page<Venue> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Lọc sân theo trạng thái (Dùng cho Admin duyệt sân).
     */
    Page<Venue> findByStatus(VenueStatus status, Pageable pageable);
}
