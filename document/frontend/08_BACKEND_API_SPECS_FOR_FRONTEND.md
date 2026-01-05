# BACKEND API SPECIFICATION (v1.0)

**System:** Badminton Booking Platform
**Base URL:** `http://localhost:8080/api/v1`
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
- **UserStatus:** `ACTIVE`, `BANNED`
- **VenueStatus:** `ACTIVE`, `INACTIVE`, `PENDING_APPROVAL`
- **BookingStatus:** `PENDING_PAY`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `CHECKED_IN`
- **PaymentMethod:** `VNPAY`, `WALLET`, `CASH`

---

## 4. API Endpoints Detail

### A. MODULE AUTH (`/auth`)

#### 1. Register
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
*   **Response (201 Created):**
    ```json
    {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyen Van A",
      "phone": "0987654321",
      "avatarUrl": null,
      "role": "USER",
      "status": "ACTIVE"
    }
    ```

#### 2. Login
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
    *   `page`: Default 0.
    *   `size`: Default 10.
    *   `sortBy`: Default "id".
    *   `sortDir`: "asc" hoặc "desc".
*   **Response:** `Page<VenueResponse>`
    ```json
    {
      "content": [
        {
          "id": 10,
          "name": "Sân Cầu Lông A",
          "description": "Sân đẹp, thoáng mát",
          "address": "123 Đường Láng",
          "province": "Hà Nội",
          "district": "Đống Đa",
          "ward": "Láng Thượng",
          "openTime": "06:00:00",
          "closeTime": "22:00:00",
          "phoneContact": "0912345678",
          "status": "ACTIVE",
          "averageRating": 4.5,
          "totalReviews": 100,
          "thumbnail": "https://img.url/1.jpg",
          "images": ["url1", "url2"],
          "courts": []
        }
      ],
      "totalPages": 5,
      "totalElements": 50,
      "last": false,
      "size": 10,
      "number": 0
    }
    ```

#### 2. Get Venue Detail (Public)
*   **Method:** `GET /public/venues/{id}`
*   **Response:** Object `VenueResponse` (như trên, nhưng kèm danh sách `courts`).

#### 3. Create Venue (Partner)
*   **Method:** `POST /partner/venues`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "name": "Sân Mới",
      "description": "Mô tả...",
      "address": "Địa chỉ...",
      "openTime": "05:00:00",    // Format HH:mm:ss
      "closeTime": "23:00:00",   // openTime < closeTime
      "phoneContact": "098..."
    }
    ```

#### 4. Get My Venues (Partner)
*   **Method:** `GET /partner/venues/me`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Response:** `Page<VenueResponse>` (Danh sách sân của chính Owner đó).

---

### C. MODULE BOOKING (`/bookings`)

#### 1. Create Booking (User)
*   **Method:** `POST /bookings`
*   **Auth:** `Bearer Token` (Role USER/OWNER)
*   **Validation Rules (Quan trọng cho Frontend):**
    *   `startTime` và `endTime` phải là **tương lai**.
    *   `startTime` < `endTime`.
    *   **Giờ Chẵn:** Phút bắt buộc phải là `00` (Ví dụ: `17:00` OK, `17:30` Lỗi).
*   **Body:**
    ```json
    {
      "courtId": 5,
      "startTime": "2025-12-27T17:00:00",
      "endTime": "2025-12-27T19:00:00",
      "paymentMethod": "VNPAY"
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "id": 101,
      "courtId": 5,
      "courtName": "Sân 1",
      "venueName": "Sân Cầu Lông A",
      "venueAddress": "123 Đường Láng",
      "startTime": "2025-12-27T17:00:00",
      "endTime": "2025-12-27T19:00:00",
      "totalPrice": 150000,
      "status": "PENDING_PAY",
      "paymentMethod": "VNPAY",
      "checkinCode": null, // Chỉ có khi CONFIRMED
      "createdAt": "..."
    }
    ```

#### 2. Get My Bookings (User)
*   **Method:** `GET /bookings/my-bookings`
*   **Auth:** `Bearer Token`
*   **Response:** `Page<BookingResponse>`

#### 3. Cancel Booking (User)
*   **Method:** `POST /bookings/{id}/cancel`
*   **Auth:** `Bearer Token`
*   **Response:** 204 No Content.

#### 4. Check-in (Partner)
*   **Method:** `POST /partner/bookings/check-in`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Body:**
    ```json
    {
      "checkinCode": "ABC123" // 6 ký tự
    }
    ```
*   **Response:** 200 OK.

#### 5. Get Owner Bookings (Partner)
*   **Method:** `GET /partner/bookings`
*   **Auth:** `Bearer Token` (Role OWNER)
*   **Response:** `Page<BookingResponse>` (Danh sách booking đổ về các sân của Owner).

---

## 5. Frontend Action Items (Dành cho Agent Frontend)

1.  **Type Definition:** Tạo các interface TypeScript (`User`, `Venue`, `Booking`, `Page`) khớp chính xác với JSON Response ở trên.
2.  **Axios Interceptor:**
    *   Tự động gắn `Authorization: Bearer ...` vào request.
    *   Xử lý lỗi `401 Unauthorized` để redirect về trang Login.
3.  **Validation Form:**
    *   Sử dụng Zod/Yup để validate form đăng ký (Email, Password min 6).
    *   Validate form đặt sân: Chặn người dùng chọn giờ lẻ (phút != 00).
4.  **Error Handling:**
    *   Khi API trả về lỗi (ví dụ 409 Conflict), hãy lấy field `message` trong JSON response để hiển thị Toast/Alert cho user.
