# BACKEND API SPECIFICATION (v2.1 - WebSocket Integrated)

**System:** Badminton Booking Platform
**Base URL:** `http://localhost:8080/api/v1`
**WebSocket URL:** `http://localhost:8080/ws`
**Date Format:** ISO 8601 (`yyyy-MM-ddTHH:mm:ss`) - Ví dụ: `2025-12-26T17:00:00`

---

## 1. Authentication & Security
*   **Mechanism:** Stateless JWT (JSON Web Token).
*   **Header:** Mọi request cần bảo mật phải đính kèm Header:
    `Authorization: Bearer <access_token>`
*   **Token Lifespan:**
    *   Access Token: 1 giờ.
    *   Refresh Token: 14 ngày.

---

## 2. Standard Error Response

Mọi lỗi (4xx, 5xx) đều trả về format JSON thống nhất:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email này đã được sử dụng: test@gmail.com",
  "path": "/api/v1/auth/register",
  "timestamp": "2025-12-26T10:30:00"
}
```

---

## 3. Enums (Frontend cần định nghĩa đúng Type)

- **Role:** `ADMIN`, `OWNER`, `USER`
- **UserStatus:** `ACTIVE`, `BANNED`, `INACTIVE`
- **VenueStatus:** `ACTIVE`, `INACTIVE`, `PENDING_APPROVAL`
- **BookingStatus:** `PENDING_PAY`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `CHECKED_IN`, `BLOCKED`
- **PaymentMethod:** `VNPAY`, `WALLET`, `CASH`
- **TransactionType:** `DEPOSIT`, `WITHDRAW`, `BOOKING_PAYMENT`, `BOOKING_REFUND`, `SETTLEMENT`
- **TransactionStatus:** `PENDING`, `SUCCESS`, `FAILED`
- **DiscountType:** `PERCENTAGE`, `FIXED_AMOUNT`

---

## 4. WebSocket Integration (Real-time Notification)

### 4.1. Connection
*   **Library:** `sockjs-client`, `stompjs`
*   **Endpoint:** `/ws`
*   **Handshake:** Cần gửi kèm Token (nếu thư viện hỗ trợ) hoặc xác thực sau khi connect.

### 4.2. Topics (Channels)
*   **User Notification:** `/queue/user/{userId}`
    *   Nhận thông báo cá nhân (Check-in thành công, Booking bị hủy...).
*   **Owner Notification:** `/queue/owner/{ownerId}`
    *   Nhận thông báo quản lý (Có booking mới, Doanh thu...).

### 4.3. Message Format
```json
{
  "id": 1,
  "title": "Đặt sân thành công",
  "message": "Bạn đã đặt sân Sân 1 thành công lúc 18:00",
  "type": "BOOKING", // BOOKING, SYSTEM, PROMOTION
  "isRead": false,
  "createdAt": "2025-12-27T10:00:00"
}
```

---

## 5. API Endpoints Detail

### A. MODULE AUTH (`/auth`)

#### 1. Register (Step 1)
*   **Method:** `POST /auth/register`
*   **Auth:** Public
*   **Body:**
    ```json
    {
      "email": "user@example.com",     // @Email, NotBlank
      "password": "password123",       // Min 6 chars
      "fullName": "Nguyen Van A",      // NotBlank
      "phone": "0987654321",
      "role": "USER"                   // hoặc "OWNER"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Vui lòng kiểm tra email để lấy mã OTP xác thực."
    }
    ```

#### 2. Verify OTP (Step 2)
*   **Method:** `POST /auth/verify-otp`
*   **Auth:** Public
*   **Body:**
    ```json
    {
      "email": "user@example.com",
      "otp": "123456"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Xác thực thành công. Bạn có thể đăng nhập ngay bây giờ."
    }
    ```

#### 3. Resend OTP
*   **Method:** `POST /auth/resend-otp`
*   **Auth:** Public
*   **Body:**
    ```json
    {
      "email": "user@example.com"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Mã OTP mới đã được gửi."
    }
    ```

#### 4. Login
*   **Method:** `POST /auth/login`
*   **Auth:** Public
*   **Body:**
    ```json
    {
      "email": "user@example.com",
      "password": "password123"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "accessToken": "eyJh...",
      "refreshToken": "uuid-string...",
      "user": { ...UserResponse... }
    }
    ```

---

### B. MODULE VENUE (`/public/venues` & `/partner/venues`)

#### 1. Search Venues (Public)
*   **Method:** `GET /public/venues`
*   **Params:**
    *   `keyword` (Optional): Tìm theo tên hoặc địa chỉ.
    *   `province` (Optional): Lọc theo Tỉnh/Thành phố.
    *   `district` (Optional): Lọc theo Quận/Huyện.
    *   `page`: Default 0.
    *   `size`: Default 10.
    *   `sortBy`: Default "id".
    *   `sortDir`: "asc" hoặc "desc".
*   **Response:** `Page<VenueResponse>`

#### 2. Get Venue Detail (Public)
*   **Method:** `GET /public/venues/{id}`
*   **Response:** Object `VenueResponse`.

#### 3. Create Venue (Partner)
*   **Method:** `POST /partner/venues`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "name": "Sân Mới",
      "description": "Mô tả...",
      "address": "Địa chỉ...",
      "province": "Hà Nội",
      "district": "Cầu Giấy",
      "ward": "Dịch Vọng",
      "openTime": "05:00:00",    // Format HH:mm:ss
      "closeTime": "23:00:00",   // openTime < closeTime
      "phoneContact": "098..."
    }
    ```

#### 4. Get My Venues (Partner)
*   **Method:** `GET /partner/venues/me`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Response:** `Page<VenueResponse>`.

#### 5. Configure Pricing (Partner)
*   **Method:** `POST /partner/venues/{id}/pricing`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    [
      {
        "courtId": null,
        "startTime": "06:00:00",
        "endTime": "16:00:00",
        "pricePerHour": 50000,
        "priority": 1,
        "daysOfWeek": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
      }
    ]
    ```

#### 6. Add Court (Partner)
*   **Method:** `POST /partner/venues/{id}/courts`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:** `{ "name": "Sân 1" }`
*   **Response:** `CourtResponse`

#### 7. Upload Images (Partner)
*   **Method:** `POST /partner/venues/{id}/images`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:** `["https://url1.jpg", "https://url2.jpg"]`
*   **Response:** 200 OK.

---

### C. MODULE BOOKING (`/bookings`)

#### 1. Create Booking (User)
*   **Method:** `POST /bookings`
*   **Auth:** `Bearer Token` (Role USER/OWNER)
*   **Body:**
    ```json
    {
      "courtId": 5,
      "startTime": "2025-12-27T17:00:00",
      "endTime": "2025-12-27T19:00:00",
      "paymentMethod": "WALLET",
      "voucherCode": "SALE50"
    }
    ```
*   **Response (201 Created):** `BookingResponse`

#### 2. Create Recurring Booking (User)
*   **Method:** `POST /bookings/recurring`
*   **Auth:** `Bearer Token` (Role USER/OWNER)
*   **Body:**
    ```json
    {
      "courtId": 5,
      "startTime": "18:00:00",
      "endTime": "20:00:00",
      "startDate": "2025-02-01",
      "endDate": "2025-02-28",
      "daysOfWeek": ["TUESDAY", "THURSDAY", "SATURDAY"],
      "paymentMethod": "WALLET"
    }
    ```
*   **Response (201 Created):** `List<BookingResponse>`

#### 3. Get My Bookings (User)
*   **Method:** `GET /bookings/my-bookings`
*   **Auth:** `Bearer Token`
*   **Response:** `Page<BookingResponse>`

#### 4. Cancel Booking (User)
*   **Method:** `POST /bookings/{id}/cancel`
*   **Auth:** `Bearer Token`
*   **Response:** 204 No Content.

#### 5. Check-in (Partner)
*   **Method:** `POST /partner/bookings/check-in`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:** `{ "checkinCode": "ABC123" }`
*   **Response:** 200 OK.

#### 6. Get Owner Bookings (Partner)
*   **Method:** `GET /partner/bookings`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Response:** `Page<BookingResponse>`.

#### 7. Block Slot (Partner)
*   **Method:** `POST /partner/bookings/block`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "courtId": 5,
      "startTime": "2025-12-27T10:00:00",
      "endTime": "2025-12-27T12:00:00",
      "reason": "Bảo trì sân"
    }
    ```
*   **Response:** 200 OK.

#### 8. Unblock Slot (Partner)
*   **Method:** `DELETE /partner/bookings/unblock/{id}`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Response:** 200 OK.

#### 9. Get Availability (Public)
*   **Method:** `GET /public/venues/{venueId}/availability`
*   **Params:** `date` (YYYY-MM-DD), `courtId` (Optional)
*   **Response:** `List<BookingResponse>`

---

### D. MODULE WALLET (`/wallets`)

#### 1. Get My Wallet
*   **Method:** `GET /wallets/me`
*   **Auth:** `Bearer Token`
*   **Response:** `{ "id": 1, "balance": 500000, "currency": "VND" }`

#### 2. Deposit (Nạp tiền)
*   **Method:** `POST /wallets/deposit`
*   **Auth:** `Bearer Token`
*   **Body:** `{ "amount": 1000000 }`
*   **Response:** 200 OK.

#### 3. Pay Booking
*   **Method:** `POST /wallets/pay-booking/{bookingId}`
*   **Auth:** `Bearer Token`
*   **Response:** 200 OK.

#### 4. Transaction History
*   **Method:** `GET /wallets/transactions`
*   **Auth:** `Bearer Token`
*   **Response:** `Page<Transaction>`

#### 5. Request Withdraw (Partner)
*   **Method:** `POST /wallets/withdraw`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "amount": 500000,
      "bankInfo": "Vietcombank - 123456789 - NGUYEN VAN A"
    }
    ```
*   **Response:** 200 OK.

---

### E. MODULE REVIEW (`/reviews`)

#### 1. Create Review
*   **Method:** `POST /reviews`
*   **Auth:** `Bearer Token`
*   **Body:** `{ "bookingId": 101, "rating": 5, "comment": "Tốt" }`
*   **Response:** 200 OK.

#### 2. Get Venue Reviews (Public)
*   **Method:** `GET /public/venues/{venueId}/reviews`
*   **Response:** `Page<Review>`

---

### F. MODULE STATISTICS & ADMIN

#### 1. Get Revenue Stats (Partner)
*   **Method:** `GET /partner/statistics/revenue`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Params:** `fromDate` (YYYY-MM-DD), `toDate` (YYYY-MM-DD)
*   **Response:** `{ "totalRevenue": 1500000 }`

#### 2. Get Venues by Status (Admin)
*   **Method:** `GET /admin/venues`
*   **Auth:** `Bearer Token` (Role ADMIN)
*   **Params:** `status` (PENDING_APPROVAL, ACTIVE, INACTIVE)
*   **Response:** `Page<VenueResponse>`

#### 3. Approve Venue (Admin)
*   **Method:** `PUT /admin/venues/{id}/approve`
*   **Auth:** `Bearer Token` (Role ADMIN)
*   **Response:** 200 OK.

#### 4. Reject Venue (Admin)
*   **Method:** `PUT /admin/venues/{id}/reject`
*   **Auth:** `Bearer Token` (Role ADMIN)
*   **Response:** 200 OK.

#### 5. Approve Withdraw (Admin)
*   **Method:** `PUT /admin/withdrawals/{id}/approve`
*   **Auth:** `Bearer Token` (Role ADMIN)
*   **Response:** 200 OK.

---

### G. MODULE PROMOTION

#### 1. Create Voucher (Partner)
*   **Method:** `POST /partner/vouchers`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "code": "SALE50",
      "discountType": "FIXED_AMOUNT",
      "discountValue": 50000,
      "minOrderValue": 200000,
      "usageLimit": 100,
      "startDate": "2025-01-01T00:00:00",
      "endDate": "2025-01-31T23:59:59",
      "venueId": 10
    }
    ```
*   **Response:** 200 OK.
