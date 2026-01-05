package com.badminton.booking.modules.venue.dto;

public record CourtResponse(
    Long id,
    String name,
    String description,
    String status
) {}
