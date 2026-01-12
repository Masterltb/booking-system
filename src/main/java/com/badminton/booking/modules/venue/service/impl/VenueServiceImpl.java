package com.badminton.booking.modules.venue.service.impl;

import com.badminton.booking.common.enums.VenueStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ConflictException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.venue.dto.CourtResponse;
import com.badminton.booking.modules.venue.dto.PricingRuleRequest;
import com.badminton.booking.modules.venue.dto.VenueRequest;
import com.badminton.booking.modules.venue.dto.VenueResponse;
import com.badminton.booking.modules.venue.entity.Court;
import com.badminton.booking.modules.venue.entity.PricingRule;
import com.badminton.booking.modules.venue.entity.Venue;
import com.badminton.booking.modules.venue.entity.VenueImage;
import com.badminton.booking.modules.venue.mapper.VenueMapper;
import com.badminton.booking.modules.venue.repository.CourtRepository;
import com.badminton.booking.modules.venue.repository.PricingRuleRepository;
import com.badminton.booking.modules.venue.repository.VenueImageRepository;
import com.badminton.booking.modules.venue.repository.VenueRepository;
import com.badminton.booking.modules.venue.service.IVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements IVenueService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final CourtRepository courtRepository;
    private final VenueImageRepository venueImageRepository;
    private final VenueMapper venueMapper;

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        if (request.openTime().isAfter(request.closeTime())) {
            throw new AppException("Giờ mở cửa phải trước giờ đóng cửa");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Venue venue = venueMapper.toEntity(request);
        venue.setOwner(owner);

        Venue savedVenue = venueRepository.save(venue);
        return venueMapper.toResponse(savedVenue);
    }

    @Override
    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
        return venueMapper.toResponse(venue);
    }

    @Override
    public Page<VenueResponse> searchVenues(String keyword, String province, String district, Pageable pageable) {
        Page<Venue> venuePage = venueRepository.search(keyword, province, district, pageable);
        return venuePage.map(venueMapper::toResponse);
    }

    @Override
    public Page<VenueResponse> getMyVenues(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Venue> venuePage = venueRepository.findByOwnerId(owner.getId(), pageable);
        return venuePage.map(venueMapper::toResponse);
    }

    @Override
    @Transactional
    public void addPricingRules(Long venueId, List<PricingRuleRequest> requests) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!venue.getOwner().getEmail().equals(email)) {
            throw new AppException("Bạn không có quyền sửa giá sân này");
        }

        for (PricingRuleRequest req : requests) {
            if (req.startTime().isAfter(req.endTime())) {
                throw new AppException("Giờ bắt đầu phải trước giờ kết thúc");
            }

            boolean isOverlap = pricingRuleRepository.existsByOverlapTime(
                    venueId, req.courtId(), req.startTime(), req.endTime());
            
            if (isOverlap) {
                throw new ConflictException("Khung giờ " + req.startTime() + "-" + req.endTime() + " bị trùng với quy tắc giá đã có.");
            }

            Court court = null;
            if (req.courtId() != null) {
                court = courtRepository.findById(req.courtId())
                        .orElseThrow(() -> new ResourceNotFoundException("Court", "id", req.courtId()));
            }

            PricingRule rule = PricingRule.builder()
                    .venue(venue)
                    .court(court)
                    .startTime(req.startTime())
                    .endTime(req.endTime())
                    .pricePerHour(req.pricePerHour())
                    .priority(req.priority() != null ? req.priority() : 1)
                    .daysOfWeek(req.daysOfWeek())
                    .build();

            pricingRuleRepository.save(rule);
        }
    }

    @Override
    @Transactional
    public CourtResponse addCourt(Long venueId, String courtName) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!venue.getOwner().getEmail().equals(email)) {
            throw new AppException("Bạn không có quyền thêm sân con vào Venue này");
        }

        Court court = Court.builder()
                .venue(venue)
                .name(courtName)
                .status("ACTIVE")
                .build();
        
        Court savedCourt = courtRepository.save(court);
        return venueMapper.toCourtResponse(savedCourt);
    }

    @Override
    @Transactional
    public void addVenueImages(Long venueId, List<String> imageUrls) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!venue.getOwner().getEmail().equals(email)) {
            throw new AppException("Bạn không có quyền sửa ảnh sân này");
        }

        for (String url : imageUrls) {
            VenueImage image = VenueImage.builder()
                    .venue(venue)
                    .imageUrl(url)
                    .isThumbnail(false)
                    .build();
            venueImageRepository.save(image);
        }
    }

    @Override
    @Transactional
    public void approveVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));
        venue.setStatus(VenueStatus.ACTIVE);
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public void rejectOrDeactivateVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));
        venue.setStatus(VenueStatus.INACTIVE);
        venueRepository.save(venue);
    }

    @Override
    public Page<VenueResponse> getVenuesByStatus(VenueStatus status, Pageable pageable) {
        // Gọi Repository tìm theo status và phân trang
        Page<Venue> venuePage = venueRepository.findByStatus(status, pageable);
        return venuePage.map(venueMapper::toResponse);
    }
}
