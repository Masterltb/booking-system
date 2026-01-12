package com.badminton.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // Kích hoạt tính năng lập lịch (Cron Job)
public class BadmintonBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadmintonBookingApplication.class, args);
	}

}
