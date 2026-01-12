package com.badminton.booking.common.config;

import com.badminton.booking.common.enums.Role;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.modules.identity.entity.User;
import com.badminton.booking.modules.identity.entity.Wallet;
import com.badminton.booking.modules.identity.repository.UserRepository;
import com.badminton.booking.modules.identity.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Class này sẽ chạy 1 lần duy nhất khi ứng dụng khởi động.
 * Dùng để tạo dữ liệu mẫu (Admin, Category...) nếu chưa có.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Kiểm tra xem đã có Admin nào chưa
        if (!userRepository.existsByEmail("admin@system.com")) {
            System.out.println("--- Đang khởi tạo tài khoản Admin mặc định ---");
            
            User admin = User.builder()
                    .email("admin@system.com")
                    .passwordHash(passwordEncoder.encode("admin123")) // Mật khẩu mặc định
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .phone("0000000000")
                    .build();
            
            User savedAdmin = userRepository.save(admin);

            // Tạo ví cho Admin (để nhận tiền phí sàn nếu cần)
            Wallet wallet = Wallet.builder()
                    .user(savedAdmin)
                    .balance(BigDecimal.ZERO)
                    .currency("VND")
                    .build();
            walletRepository.save(wallet);
            
            System.out.println("--- Đã tạo Admin: admin@system.com / admin123 ---");
        }
    }
}
