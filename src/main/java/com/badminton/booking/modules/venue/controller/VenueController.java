package com.badminton.booking.modules.venue.controller;

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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VenueController {

    private static final Logger logger = LoggerFactory.getLogger(VenueController.class);
    private final IVenueService venueService;

    // --- PUBLIC API (Ai cũng gọi được) ---

    /**
     * Tìm kiếm sân cầu lông.
     * Endpoint: GET /api/v1/public/venues
     */
    @GetMapping("/public/venues")
    public ResponseEntity<Page<VenueResponse>> searchVenues(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(venueService.searchVenues(keyword, pageable));
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
    @PreAuthorize("hasRole('OWNER')") // Bảo vệ 2 lớp (SecurityConfig + Annotation)
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
}
