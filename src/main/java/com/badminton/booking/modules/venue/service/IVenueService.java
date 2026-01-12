package com.badminton.booking.modules.venue.service;

import com.badminton.booking.common.enums.VenueStatus;
import com.badminton.booking.modules.venue.dto.CourtResponse;
import com.badminton.booking.modules.venue.dto.PricingRuleRequest;
import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVenueService {

    VenueResponse createVenue(VenueRequest request);
    VenueResponse getVenueById(Long id);
    Page<VenueResponse> searchVenues(String keyword, String province, String district, Pageable pageable);
    Page<VenueResponse> getMyVenues(Pageable pageable);
    void addPricingRules(Long venueId, List<PricingRuleRequest> requests);
    CourtResponse addCourt(Long venueId, String courtName);
    void addVenueImages(Long venueId, List<String> imageUrls);
    void approveVenue(Long venueId);
    void rejectOrDeactivateVenue(Long venueId);

    /**
     * Lấy danh sách sân theo trạng thái (Dành cho Admin).
     * @param status Trạng thái cần lọc (PENDING_APPROVAL, ACTIVE, INACTIVE).
     * @param pageable Phân trang.
     * @return Danh sách sân.
     */
    Page<VenueResponse> getVenuesByStatus(VenueStatus status, Pageable pageable);
}
