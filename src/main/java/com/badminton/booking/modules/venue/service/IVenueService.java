package com.badminton.booking.modules.venue.service;

import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IVenueService {

    /**
     * Tạo mới một cụm sân (Venue).
     * @param request DTO chứa thông tin sân.
     * @return DTO phản hồi (bao gồm ID vừa tạo).
     */
    VenueResponse createVenue(VenueRequest request);

    /**
     * Lấy chi tiết một sân theo ID.
     * @param id ID của sân.
     * @return DTO chi tiết.
     */
    VenueResponse getVenueById(Long id);

    /**
     * Tìm kiếm sân (Public API).
     * @param keyword Tên hoặc địa chỉ.
     * @param pageable Phân trang.
     * @return Danh sách sân (Page).
     */
    Page<VenueResponse> searchVenues(String keyword, Pageable pageable);

    /**
     * Lấy danh sách sân của chính Owner đang đăng nhập.
     * @param pageable Phân trang.
     * @return Danh sách sân của Owner.
     */
    Page<VenueResponse> getMyVenues(Pageable pageable);
}
