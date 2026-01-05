# 02_API_SPECIFICATION.md

**Mục đích:** Quy định chính xác đầu vào/đầu ra cho các API.

## 1. Global Response
```json
{
  "status": 200,
  "message": "Success",
  "data": { ... },
  "timestamp": "2025-12-26T10:00:00"
}
```

---

## 2. Module Auth
- `POST /api/v1/auth/register`: Đăng ký.
- `POST /api/v1/auth/login`: Đăng nhập.
- `POST /api/v1/auth/refresh-token`: Lấy Access Token mới từ Refresh Token.
- `POST /api/v1/auth/logout`: Đăng xuất (Xóa Refresh Token).

---

## 3. Module Venue (Public & Owner)

### 3.1. Tìm kiếm Sân (Public)
`GET /api/v1/venues/search`
**Params:**
- `keyword`: Tên sân, địa chỉ.
- `province`, `district`: Lọc theo địa điểm.
- `lat`, `lon`, `radius`: Tìm quanh đây (Option).

**Response:**
```json
[
  {
    "id": 1,
    "name": "Sân Cầu Lông A",
    "address": "123 Đường B",
    "minPrice": 50000,
    "maxPrice": 80000,
    "rating": 4.5,
    "thumbnail": "url..."
  }
]
```

### 3.2. Chi tiết Sân (Public)
`GET /api/v1/venues/{id}`
- Trả về thông tin chi tiết, danh sách sân con (courts), hình ảnh, và review.

### 3.3. Quản lý Sân (Owner)
- `POST /api/v1/venues`: Tạo cụm sân mới.
- `PUT /api/v1/venues/{id}`: Cập nhật thông tin.
- `POST /api/v1/venues/{id}/courts`: Thêm sân con.
- `POST /api/v1/venues/{id}/pricing`: Cấu hình bảng giá.

---

## 4. Module Booking (Core)

### 4.1. Xem Lịch Trống
`GET /api/v1/venues/{venueId}/availability?date=2025-12-26`
- Trả về các slot còn trống của từng sân con trong ngày.

### 4.2. Đặt Sân
`POST /api/v1/bookings`
**Body:**
```json
{
  "courtId": 1,
  "startTime": "2025-12-26T17:00:00",
  "endTime": "2025-12-26T18:00:00",
  "paymentMethod": "VNPAY" // hoặc "WALLET"
}
```

### 4.3. Lịch sử Đặt Sân (User)
`GET /api/v1/bookings/my-bookings`
- Filter: `status` (UPCOMING, COMPLETED, CANCELLED).

### 4.4. Check-in (Owner)
`POST /api/v1/bookings/check-in`
- Body: `{ "checkinCode": "XYZ123" }`

---

## 5. Module Wallet & Payment

### 5.1. Xem Số Dư
`GET /api/v1/wallets/me`

### 5.2. Lịch sử Giao dịch
`GET /api/v1/wallets/transactions`

### 5.3. Nạp tiền vào Ví (Deposit)
`POST /api/v1/wallets/deposit`
- Body: `{ "amount": 500000, "method": "VNPAY" }`
- Response: Payment URL.

---

## 6. Module Review & Notification

### 6.1. Viết Đánh giá
`POST /api/v1/reviews`
- Body: `{ "bookingId": 101, "rating": 5, "comment": "Sân đẹp" }`
- Constraint: Chỉ được review khi booking status là `COMPLETED` hoặc `CHECKED_IN`.

### 6.2. Lấy Thông báo
`GET /api/v1/notifications`
- `PUT /api/v1/notifications/{id}/read`: Đánh dấu đã đọc.
