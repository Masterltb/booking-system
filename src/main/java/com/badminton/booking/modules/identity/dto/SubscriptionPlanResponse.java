package com.badminton.booking.modules.identity.dto;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(
    Long id,
    String name,
    BigDecimal price,
    Integer durationDays,
    Integer maxVenues,
    String description
) {}
