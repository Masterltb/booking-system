package com.badminton.booking.modules.venue.controller;

import com.badminton.booking.common.enums.VenueStatus;
import com.badminton.booking.modules.identity.service.IWalletService;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import com.badminton.booking.modules.venue.service.IVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IVenueService venueService;
    private final IWalletService walletService; // Inject thêm WalletService

    @GetMapping("/venues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<VenueResponse>> getVenuesByStatus(
            @RequestParam(defaultValue = "PENDING_APPROVAL") VenueStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(venueService.getVenuesByStatus(status, pageable));
    }

    @PutMapping("/venues/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveVenue(@PathVariable Long id) {
        venueService.approveVenue(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/venues/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectVenue(@PathVariable Long id) {
        venueService.rejectOrDeactivateVenue(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Duyệt yêu cầu rút tiền.
     * Endpoint: PUT /api/v1/admin/withdrawals/{id}/approve
     */
    @PutMapping("/withdrawals/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveWithdraw(@PathVariable Long id) {
        walletService.approveWithdraw(id);
        return ResponseEntity.ok().build();
    }
}
