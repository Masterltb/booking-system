package com.badminton.booking.modules.identity.mapper;

import com.badminton.booking.modules.identity.dto.RegisterRequest;
import com.badminton.booking.modules.identity.dto.UserResponse;
import com.badminton.booking.modules.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "passwordHash", ignore = true) // Password sẽ được mã hóa riêng
    @Mapping(target = "status", constant = "ACTIVE") // Mặc định Active khi đăng ký
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}
