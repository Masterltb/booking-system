package com.badminton.booking.modules.venue.service.impl;

import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import com.badminton.booking.modules.venue.entity.Venue;
import com.badminton.booking.modules.venue.mapper.VenueMapper;
import com.badminton.booking.modules.venue.repository.VenueRepository;
import com.badminton.booking.modules.venue.service.IVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements IVenueService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final VenueMapper venueMapper;

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        // 1. Validate Logic: Giờ mở cửa phải trước giờ đóng cửa
        if (request.openTime().isAfter(request.closeTime())) {
            throw new AppException("Giờ mở cửa phải trước giờ đóng cửa");
        }

        // 2. Lấy thông tin Owner từ Security Context (Token)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Map DTO -> Entity
        Venue venue = venueMapper.toEntity(request);
        
        // 4. Gán Owner cho Venue
        venue.setOwner(owner);

        // 5. Lưu vào DB
        Venue savedVenue = venueRepository.save(venue);

        // 6. Trả về Response
        return venueMapper.toResponse(savedVenue);
    }

    @Override
    public VenueResponse getVenueById(Long id) {
        // Sử dụng hàm findWithDetailsById để fetch luôn images và courts (tránh N+1)
        Venue venue = venueRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
        
        return venueMapper.toResponse(venue);
    }

    @Override
    public Page<VenueResponse> searchVenues(String keyword, Pageable pageable) {
        // Gọi Repository tìm kiếm (chỉ lấy sân ACTIVE)
        Page<Venue> venuePage = venueRepository.search(keyword, pageable);
        
        // Map Page<Venue> -> Page<VenueResponse>
        return venuePage.map(venueMapper::toResponse);
    }

    @Override
    public Page<VenueResponse> getMyVenues(Pageable pageable) {
        // 1. Lấy ID của Owner đang đăng nhập
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Tìm sân theo Owner ID
        Page<Venue> venuePage = venueRepository.findByOwnerId(owner.getId(), pageable);
        
        return venuePage.map(venueMapper::toResponse);
    }
}
