package com.badminton.booking.modules.venue.mapper;

import com.badminton.booking.modules.venue.dto.CourtResponse;
import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import com.badminton.booking.modules.venue.entity.Court;
import com.badminton.booking.modules.venue.entity.Venue;
import com.badminton.booking.modules.venue.entity.VenueImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    @Mapping(target = "thumbnail", source = "images", qualifiedByName = "mapThumbnail")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImageUrls")
    // MapStruct tự động convert Set -> List cho courts
    VenueResponse toResponse(Venue venue);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE") 
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "totalReviews", constant = "0")
    @Mapping(target = "courts", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Venue toEntity(VenueRequest request);

    CourtResponse toCourtResponse(Court court);

    @Named("mapThumbnail")
    default String mapThumbnail(Set<VenueImage> images) { // Đổi List -> Set
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .map(VenueImage::getImageUrl)
                .orElse(images.iterator().next().getImageUrl()); // Lấy phần tử đầu tiên của Set
    }

    @Named("mapImageUrls")
    default List<String> mapImageUrls(Set<VenueImage> images) { // Đổi List -> Set
        if (images == null) return Collections.emptyList();
        return images.stream()
                .map(VenueImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
