package com.badminton.booking.modules.identity.repository;

import com.badminton.booking.modules.identity.entity.RefreshToken;
import com.badminton.booking.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Xóa token của user khi Logout hoặc khi token hết hạn.
     * @Modifying cần thiết cho các query UPDATE/DELETE.
     */
    @Modifying
    int deleteByUser(User user);
}
