package com.badminton.booking.modules.venue.controller;

import com.badminton.booking.modules.venue.dto.CourtResponse;
import com.badminton.booking.modules.venue.dto.PricingRuleRequest;
import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import com.badminton.booking.modules.venue.service.IVenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VenueController {

    private static final Logger logger = LoggerFactory.getLogger(VenueController.class);
    private final IVenueService venueService;

    // --- PUBLIC API (Ai cũng gọi được) ---

    /**
     * Tìm kiếm sân cầu lông (Nâng cấp).
     * Endpoint: GET /api/v1/public/venues
     */
    @GetMapping("/public/venues")
    public ResponseEntity<Page<VenueResponse>> searchVenues(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(venueService.searchVenues(keyword, province, district, pageable));
    }

    /**
     * Xem chi tiết sân.
     * Endpoint: GET /api/v1/public/venues/{id}
     */
    @GetMapping("/public/venues/{id}")
    public ResponseEntity<VenueResponse> getVenueDetail(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    // --- PARTNER API (Chỉ Owner gọi được) ---

    /**
     * Tạo sân mới.
     * Endpoint: POST /api/v1/partner/venues
     */
    @PostMapping("/partner/venues")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        logger.info("Owner đang tạo sân mới: {}", request.name());
        VenueResponse response = venueService.createVenue(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Lấy danh sách sân của tôi (Owner).
     * Endpoint: GET /api/v1/partner/venues/me
     */
    @GetMapping("/partner/venues/me")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<VenueResponse>> getMyVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(venueService.getMyVenues(pageable));
    }

    /**
     * Cấu hình bảng giá cho sân.
     * Endpoint: POST /api/v1/partner/venues/{id}/pricing
     */
    @PostMapping("/partner/venues/{id}/pricing")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> addPricingRules(
            @PathVariable Long id,
            @Valid @RequestBody List<PricingRuleRequest> requests
    ) {
        venueService.addPricingRules(id, requests);
        return ResponseEntity.ok().build();
    }

    /**
     * Thêm sân con (Court) vào Venue.
     * Endpoint: POST /api/v1/partner/venues/{id}/courts
     */
    @PostMapping("/partner/venues/{id}/courts")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CourtResponse> addCourt(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String courtName = request.get("name");
        CourtResponse response = venueService.addCourt(id, courtName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Upload ảnh sân.
     * Endpoint: POST /api/v1/partner/venues/{id}/images
     */
    @PostMapping("/partner/venues/{id}/images")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> addVenueImages(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls
    ) {
        venueService.addVenueImages(id, imageUrls);
        return ResponseEntity.ok().build();
    }
}
