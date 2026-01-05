package com.badminton.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Kích hoạt tính năng tự động ghi nhận thời gian (Auditing)
public class BadmintonBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadmintonBookingApplication.class, args);
	}

}
