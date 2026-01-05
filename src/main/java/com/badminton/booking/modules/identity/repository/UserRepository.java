package com.badminton.booking.modules.identity.repository;

import com.badminton.booking.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Dùng cho luồng Login (Auth Flow 1.1).
     * Index 'unique' trên cột email giúp query này cực nhanh.
     */
    Optional<User> findByEmail(String email);

    /**
     * Dùng cho luồng Register (Auth Flow).
     * Kiểm tra xem email đã được đăng ký chưa trước khi tạo user mới.
     */
    boolean existsByEmail(String email);
}
