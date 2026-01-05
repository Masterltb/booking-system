package com.badminton.booking.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Dùng cho các lỗi Bad Request (400) chung chung.
 * Ví dụ: Logic nghiệp vụ sai, tham số không hợp lệ.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
