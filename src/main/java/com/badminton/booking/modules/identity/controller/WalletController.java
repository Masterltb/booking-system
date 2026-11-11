package com.badminton.booking.modules.identity.controller;

import com.badminton.booking.modules.identity.dto.WalletResponse;
import com.badminton.booking.modules.identity.entity.Transaction;
import com.badminton.booking.modules.identity.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final IWalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {
        return ResponseEntity.ok(walletService.getMyWallet());
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestBody Map<String, BigDecimal> request) {
        // Deposits must only be credited after a verified provider callback.
        // This project has no provider integration, so fail closed instead of
        // trusting an amount supplied by the client.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/pay-booking/{bookingId}")
    public ResponseEntity<Void> payBooking(@PathVariable Long bookingId) {
        walletService.payBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(walletService.getMyTransactions(pageable));
    }

    /**
     * Yêu cầu rút tiền (Chỉ Owner).
     * Endpoint: POST /api/v1/wallets/withdraw
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> requestWithdraw(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String bankInfo = (String) request.get("bankInfo");
        
        walletService.requestWithdraw(amount, bankInfo);
        return ResponseEntity.ok().build();
    }
}
