package com.badminton.booking.modules.venue.dto;

import com.badminton.booking.common.enums.VenueStatus;
import java.time.LocalTime;
import java.util.List;

public record VenueResponse(
    Long id,
    String name,
    String description,
    String address,
    String province,
    String district,
    String ward,
    LocalTime openTime,
    LocalTime closeTime,
    String phoneContact,
    VenueStatus status,
    Double averageRating, // Tính toán từ Review
    Integer totalReviews,
    String thumbnail,     // Ảnh đại diện
    List<String> images,  // Danh sách ảnh
    List<CourtResponse> courts // Danh sách sân con
) {}
