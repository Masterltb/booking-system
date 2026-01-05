package com.badminton.booking.modules.identity.dto;

import java.math.BigDecimal;

public record WalletResponse(
    Long id,
    BigDecimal balance,
    String currency
) {}
