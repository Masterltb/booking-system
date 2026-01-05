# 04_SECURITY_POLICY.md

## 1. Password
- **Thuật toán:** BCrypt.
- **Độ mạnh (Cost factor):** 10 hoặc 12.

## 2. Token (JWT)
- **Thuật toán ký:** HMAC-SHA256.
- **Secret Key:** Tối thiểu 256 bit (32 ký tự ngẫu nhiên).
- **Access Token TTL:** 1 giờ.
- **Refresh Token TTL:** 14 ngày.

## 3. API Security
- Mọi API bắt đầu bằng `/api/v1/admin/**` -> Yêu cầu Role **ADMIN**.
- Mọi API bắt đầu bằng `/api/v1/partner/**` -> Yêu cầu Role **OWNER**.
- User chỉ được xem/sửa Booking của chính mình (Check ID trong Service).

## 4. Data Security
- Không log thông tin nhạy cảm (Password, Token) ra console.
