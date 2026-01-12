package com.badminton.booking.common.service;

import com.badminton.booking.modules.booking.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Xác nhận đặt sân thành công - Mã: " + booking.getCheckinCode());

            Context context = new Context();
            context.setVariable("name", booking.getUser().getFullName());
            context.setVariable("venueName", booking.getCourt().getVenue().getName());
            context.setVariable("courtName", booking.getCourt().getName());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            String timeStr = booking.getStartTime().format(formatter) + " - " + booking.getEndTime().format(formatter);
            context.setVariable("time", timeStr);
            
            context.setVariable("checkinCode", booking.getCheckinCode());
            context.setVariable("totalPrice", booking.getTotalPrice());

            String htmlContent = templateEngine.process("booking-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Đã gửi email xác nhận cho booking ID: {}", booking.getId());

        } catch (MessagingException e) {
            logger.error("Lỗi khi gửi email: {}", e.getMessage());
        }
    }

    /**
     * Gửi email OTP (MỚI).
     */
    @Async
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Badminton Booking");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);

            String htmlContent = templateEngine.process("otp-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Đã gửi OTP cho email: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Lỗi khi gửi email OTP: {}", e.getMessage());
        }
    }
}
