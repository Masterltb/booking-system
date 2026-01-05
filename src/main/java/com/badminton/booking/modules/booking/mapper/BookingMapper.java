package com.badminton.booking.modules.booking.mapper;

import com.badminton.booking.modules.booking.dto.BookingResponse;
import com.badminton.booking.modules.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "courtId", source = "court.id")
    @Mapping(target = "courtName", source = "court.name")
    @Mapping(target = "venueName", source = "court.venue.name")
    @Mapping(target = "venueAddress", source = "court.venue.address")
    BookingResponse toResponse(Booking booking);
}
