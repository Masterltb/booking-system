package com.badminton.booking.modules.identity.mapper;

import com.badminton.booking.modules.identity.dto.SubscriptionPlanResponse;
import com.badminton.booking.modules.identity.entity.SubscriptionPlan;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionPlanResponse toResponse(SubscriptionPlan plan);
}
