# TÀI LIỆU ĐẶC TẢ LUỒNG NGHIỆP VỤ CHI TIẾT

**Project:** Badminton Booking System  
**Version:** 1.1 (Hybrid Model)  
**Format:** Markdown  
**Last Updated:** 2025-12-26

---

## 1. LUỒNG XÁC THỰC (AUTHENTICATION FLOW)
**Mục tiêu:** Đảm bảo chỉ người dùng đã đăng ký mới được truy cập tài nguyên. Sử dụng cơ chế Stateless JWT.

### 1.1. Đăng nhập (Login)
- **Endpoint:** `POST /api/v1/auth/login`
- **Input:** `email`, `password`

**Quy trình xử lý (Backend):**
1. **Query DB:** `SELECT * FROM users WHERE email = ?`. Nếu không thấy -> Trả lỗi `404 Not Found`.
2. **Check Password:** Dùng `BCrypt.checkpw(input_pass, db_hash)`. Nếu sai -> Trả lỗi `401 Bad Credentials`.
3. **Check Status:** Nếu `status == BANNED` -> Trả lỗi `403 Account Locked`.
4. **Generate Token:**
   - **Access Token:** Chứa `sub=email`, `role=USER/OWNER`, `iat`, `exp` (1 giờ). Ký bằng HS256.
   - **Refresh Token:** UUID ngẫu nhiên, lưu xuống DB bảng `refresh_tokens` với hạn 14 ngày.

**Output:**
```json
{ "accessToken": "...", "refreshToken": "...", "user": { ... } }
```

### 1.2. Bảo vệ API (Middleware/Filter)
- **Áp dụng:** Tất cả API trừ `/auth/**` và `/public/**`.

**Logic:**
1. Lấy Header `Authorization`. Check format `Bearer <token>`.
2. **Validate Signature:** Dùng SecretKey để verify. Nếu sai/hết hạn -> `401 Unauthorized`.
3. **Set Context:** Lưu thông tin User vào `SecurityContextHolder` để Controller sử dụng.

---

## 2. LUỒNG ĐẶT SÂN & THANH TOÁN (CORE BOOKING FLOW)
**Mục tiêu:** Xử lý đặt sân chính xác, tuyệt đối không trùng lịch (Race Condition), và tích hợp VNPay.

### 2.1. Kiểm tra & Khóa Slot (Create Booking)
- **Endpoint:** `POST /api/v1/bookings`
- **Input:** `courtId`, `startTime` (ISO8601), `endTime`.

**Quy trình xử lý (Backend):**
1. **Validate Time:** `startTime` phải > `now`. `endTime - startTime` >= 30 phút.
2. **Concurrency Lock (Quan trọng):**
   - Tạo Redis Key: `LOCK_BOOKING_{courtId}_{startTime}_{endTime}`.
   - Nếu Key tồn tại -> Trả lỗi `409 Conflict` ("Sân này đang được người khác giữ chỗ").
   - Nếu chưa -> Set Key với TTL = 15 phút (Thời gian cho phép thanh toán).
3. **Double Check DB:** Query bảng `bookings` xem có record nào chồng lấn thời gian và `status != CANCELLED` không.
4. **Calculate Fee:** `platform_fee = total_price * 0.05` (5%).
5. **Create Record:** Insert vào DB với `status = PENDING_PAY`, `platform_fee = ...`.
6. **Call Payment Gateway:**
   - Tạo URL thanh toán VNPay với `vnp_TxnRef = bookingId`.
   - `vnp_ReturnUrl`: Link Frontend trang "Kết quả".

**Output:**
```json
{ "paymentUrl": "https://sandbox.vnpayment.vn/..." }
```

### 2.2. Xử lý Kết quả Thanh toán (IPN Callback)
- **Endpoint:** `GET/POST /api/v1/payments/vnpay-ipn` (Gọi bởi Server VNPay)

**Quy trình xử lý:**
1. **Validate Checksum:** Dùng `vnp_HashSecret` để hash lại dữ liệu và so sánh với `vnp_SecureHash`. Nếu sai -> Dừng xử lý (nghi ngờ hack).
2. **Get Booking:** Tìm booking theo `vnp_TxnRef`.
3. **Check Amount:** So sánh số tiền VNPay báo về với `booking.total_price`.
4. **Update State:**
   - **Nếu `vnp_ResponseCode == 00` (Thành công):**
     - Update `booking.status = CONFIRMED`.
     - Tạo `checkin_code` (Random 6 ký tự).
     - Update `wallets` (System Holding): `balance += amount`.
     - Insert `transactions`: Type `BOOKING_PAYMENT`.
   - **Nếu Thất bại:**
     - Update `booking.status = CANCELLED`.
     - Xóa Redis Lock.

---

## 3. LUỒNG CHECK-IN & ĐỐI SOÁT (SETTLEMENT FLOW)
**Mục tiêu:** Đảm bảo người dùng đến sân thật sự và chuyển tiền cho chủ sân (đã trừ phí).

### 3.1. Check-in tại quầy
- **Actor:** Chủ sân (Owner).
- **Endpoint:** `POST /api/v1/bookings/check-in`
- **Input:** `checkinCode` (do khách đọc hoặc quét QR).

**Quy trình xử lý:**
1. Tìm Booking có `checkin_code = input` VÀ `court_id` thuộc về Owner này.
2. **Validate:**
   - Nếu `status != CONFIRMED` -> Lỗi.
   - Nếu `startTime` quá xa (ví dụ check-in sớm 5 tiếng) -> Warning hoặc Block tùy config.
3. **Update:** `status = CHECKED_IN`.

**Output:**
```json
{ "success": true, "customerName": "Nguyen Van A", "court": "Sân 1" }
```

### 3.2. Đối soát tự động (Cron Job Settlement)
- **Trigger:** Chạy mỗi 30 phút (Spring Scheduler).

**Logic:**
1. **Query DB:** Tìm các booking thỏa mãn 2 điều kiện:
   - `status` là `CHECKED_IN` (Khách đã đến) **HOẶC** `CONFIRMED` nhưng `endTime` đã qua 2 tiếng (Khách không đến nhưng không hủy -> Mặc định vẫn tính tiền).
   - `is_settled = FALSE` (Chưa trả tiền chủ sân).
2. **Transaction (Bắt buộc Atomic):**
   - **Tính toán:** `owner_receive = total_price - platform_fee`.
   - **System Wallet:** Trừ tiền (`-owner_receive`). (Phần dư `platform_fee` ở lại ví System làm doanh thu).
   - **Owner Wallet:** Cộng tiền (`+owner_receive`).
   - Insert Log vào `transactions`.
   - Update `booking.is_settled = TRUE`, `booking.status = COMPLETED`.

---

## 4. LUỒNG HỦY SÂN & HOÀN TIỀN (REFUND FLOW)
**Mục tiêu:** Xử lý hủy sân theo chính sách cấu hình sẵn.

### 4.1. Khách hủy sân
- **Endpoint:** `POST /api/v1/bookings/{id}/cancel`

**Quy trình xử lý:**
1. Get Booking. Nếu `status != CONFIRMED` -> Lỗi.
2. **Calculate Refund:**
   - Lấy `rule_cancel_policy` của Venue.
   - Tính khoảng cách `time_diff = startTime - now`.
   - Ví dụ: > 24h hoàn 100%, > 12h hoàn 50%, < 12h hoàn 0%.
3. **Process Money:**
   - Nếu hoàn > 0: Gọi API hoàn tiền VNPay (Refund API) hoặc cộng tiền vào Ví User (nếu có tính năng ví).
   - Trừ tiền System Holding Wallet.
4. **Update:** `status = CANCELLED`. `refund_amount = ...`.
5. Xóa Redis Lock (để người khác đặt).

---

## 5. LUỒNG QUẢN LÝ SÂN (VENUE CONFIGURATION)
**Mục tiêu:** Thiết lập giá linh động.

### 5.1. Cấu hình Giá (Pricing Rules)
- **Endpoint:** `POST /api/v1/venues/{id}/pricing`
- **Input:**
```json
[
  { "start": "06:00", "end": "10:00", "price": 50000 },
  { "start": "17:00", "end": "21:00", "price": 80000 }
]
```

**Validation Logic:**
1. **Quy tắc 1:** `start` phải < `end`.
2. **Quy tắc 2 (No Overlap):** Khoảng thời gian mới không được trùng với khoảng đã có.
   - **Thuật toán:** Sort các khoảng theo giờ bắt đầu. Duyệt vòng lặp: Nếu `end` của khoảng trước > `start` của khoảng sau -> Lỗi Trùng Giờ.
3. **Save DB:** Lưu vào bảng `pricing_rules`.

---

## 6. DANH SÁCH MÃ LỖI (ERROR CODES STANDARD)
Backend sẽ trả về `errorCode` cụ thể trong JSON để Frontend hiển thị thông báo đa ngôn ngữ.

| Code | HTTP Status | Message (Dev) | Message (User Friendly) |
| :--- | :--- | :--- | :--- |
| `AUTH_001` | 401 | Invalid credentials | Email hoặc mật khẩu không đúng. |
| `AUTH_002` | 403 | Account banned | Tài khoản của bạn đã bị khóa. |
| `BOOK_001` | 409 | Slot occupied | Khung giờ này vừa có người đặt, vui lòng chọn giờ khác. |
| `BOOK_002` | 400 | Invalid time range | Thời gian đặt sân không hợp lệ (tối thiểu 30p). |
| `PAY_001` | 402 | Payment failed | Thanh toán thất bại hoặc bị hủy. |
| `VNU_001` | 404 | Venue closed | Sân đã đóng cửa vào giờ này. |
| `SYS_001` | 500 | Internal error | Lỗi hệ thống, vui lòng thử lại sau. |
