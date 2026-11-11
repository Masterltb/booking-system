package com.badminton.booking.modules.identity.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.badminton.booking.modules.identity.service.IWalletService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class WalletControllerTest {

    @Test
    void depositIsUnavailableUntilProviderVerificationExists() {
        IWalletService walletService = mock(IWalletService.class);
        WalletController controller = new WalletController(walletService);

        var response = controller.deposit(Map.of("amount", new BigDecimal("100000")));

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
        verifyNoInteractions(walletService);
    }
}
