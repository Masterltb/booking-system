# 09_USE_CASE_SPECIFICATION.md

**Project:** Badminton Booking System  
**Version:** 1.1 (OTP Integrated)  
**Last Updated:** 2025-01-11

---

## 1. ACTORS (TÁC NHÂN)

| Actor | Mô tả |
| :--- | :--- |
| **Guest (Khách vãng lai)** | Người dùng chưa đăng nhập. Chỉ có thể xem thông tin công khai. |
| **User (Người đặt sân)** | Người dùng đã đăng ký tài khoản. Có thể đặt sân, thanh toán, đánh giá. |
| **Owner (Chủ sân)** | Đối tác kinh doanh. Có quyền tạo sân, quản lý lịch đặt, cấu hình giá. |
| **Admin (Quản trị viên)** | Quản lý toàn bộ hệ thống, duyệt sân, quản lý người dùng. |
| **System (Hệ thống)** | Các tác vụ tự động (Cron Job) như hủy đơn quá hạn, đối soát doanh thu. |

---

## 2. USE CASE DIAGRAM (MÔ TẢ)

### 2.1. Nhóm Guest
*   **UC01:** Đăng ký (Register) - **Có xác thực OTP**
*   **UC02:** Đăng nhập (Login)
*   **UC03:** Tìm kiếm sân (Search Venues)
*   **UC04:** Xem chi tiết sân (View Venue Detail)

### 2.2. Nhóm User (Kế thừa Guest)
*   **UC05:** Đặt sân (Create Booking)
*   **UC06:** Thanh toán (Make Payment)
*   **UC07:** Hủy đặt sân (Cancel Booking)
*   **UC08:** Xem lịch sử đặt sân (View Booking History)
*   **UC09:** Đánh giá sân (Review Venue)
*   **UC10:** Nạp tiền vào ví (Deposit Wallet)

### 2.3. Nhóm Owner (Kế thừa User)
*   **UC11:** Tạo sân mới (Create Venue)
*   **UC12:** Thêm sân con (Add Court)
*   **UC13:** Cấu hình giá (Configure Pricing)
*   **UC14:** Quản lý lịch đặt (Manage Bookings)
*   **UC15:** Check-in cho khách (Check-in User)
*   **UC16:** Xem báo cáo doanh thu (View Revenue)

### 2.4. Nhóm Admin
*   **UC17:** Duyệt sân (Approve Venue)
*   **UC18:** Khóa tài khoản (Ban User)

---

## 3. DETAILED USE CASE SPECIFICATION (CHI TIẾT)

### UC01: Đăng ký (Register with OTP)
*   **Actor:** Guest
*   **Main Flow:**
    1.  Guest nhập thông tin đăng ký (Email, Password, Name).
    2.  Hệ thống kiểm tra Email chưa tồn tại.
    3.  Hệ thống tạo tài khoản tạm với trạng thái `INACTIVE`.
    4.  Hệ thống sinh mã OTP (6 số) và gửi qua Email.
    5.  Hệ thống yêu cầu Guest nhập mã OTP.
    6.  Guest nhập mã OTP.
    7.  Hệ thống xác thực OTP.
    8.  Nếu đúng, hệ thống kích hoạt tài khoản (`ACTIVE`) và tạo Ví.
    9.  Thông báo đăng ký thành công.
*   **Alternative Flow (Sai OTP):**
    *   Hệ thống báo lỗi. Cho phép nhập lại hoặc gửi lại mã.

### UC05: Đặt sân (Create Booking)
*   **Actor:** User
*   **Pre-condition:** User đã đăng nhập. Sân còn trống trong khung giờ chọn.
*   **Main Flow:**
    1.  User chọn sân, ngày, giờ bắt đầu, giờ kết thúc trên giao diện.
    2.  Hệ thống kiểm tra tính hợp lệ (Giờ chẵn, Thời gian tương lai).
    3.  Hệ thống kiểm tra trùng lịch (Availability Check).
    4.  Hệ thống tính toán tổng tiền (bao gồm phí sàn).
    5.  User chọn phương thức thanh toán (Ví hoặc VNPay).
    6.  User xác nhận đặt.
    7.  Hệ thống tạo Booking với trạng thái `PENDING_PAY`.
    8.  Hệ thống chuyển hướng sang trang thanh toán (nếu chọn VNPay) hoặc trừ tiền ví (nếu chọn Wallet).
*   **Alternative Flow (Lỗi trùng lịch):**
    *   Tại bước 3, nếu sân đã bị người khác đặt -> Hệ thống báo lỗi "Sân đã có người đặt". User chọn giờ khác.
*   **Post-condition:** Booking được tạo. Slot giờ đó bị khóa.

---

### UC13: Cấu hình giá (Configure Pricing)
*   **Actor:** Owner
*   **Pre-condition:** Owner sở hữu sân đó.
*   **Main Flow:**
    1.  Owner chọn sân cần cấu hình.
    2.  Owner nhập: Khung giờ (VD: 17h-20h), Giá tiền (VD: 80k), Ngày áp dụng (VD: T7, CN).
    3.  Hệ thống kiểm tra xem khung giờ mới có bị trùng lặp (Overlap) với các Rule cũ không.
    4.  Nếu hợp lệ, hệ thống lưu Rule vào DB.
*   **Alternative Flow (Lỗi trùng lặp):**
    *   Tại bước 3, nếu trùng -> Hệ thống báo lỗi "Khung giờ này đã có cấu hình giá". Owner phải sửa lại hoặc xóa Rule cũ.

---

### UC15: Check-in cho khách (Check-in User)
*   **Actor:** Owner
*   **Pre-condition:** Khách đến sân và đưa mã Check-in.
*   **Main Flow:**
    1.  Owner nhập mã Check-in (6 ký tự) vào hệ thống.
    2.  Hệ thống tìm kiếm Booking tương ứng.
    3.  Hệ thống kiểm tra trạng thái Booking (Phải là `CONFIRMED`).
    4.  Hệ thống kiểm tra xem Booking này có thuộc sân của Owner không.
    5.  Nếu hợp lệ, hệ thống đổi trạng thái sang `CHECKED_IN`.
    6.  Hệ thống thông báo "Check-in thành công".
*   **Alternative Flow (Mã sai):**
    *   Hệ thống báo "Mã không tồn tại".
*   **Alternative Flow (Sai trạng thái):**
    *   Nếu Booking chưa thanh toán hoặc đã hủy -> Hệ thống báo lỗi.

---

### UC06: Thanh toán (Make Payment - Wallet)
*   **Actor:** User
*   **Pre-condition:** User có đủ số dư trong ví. Booking đang ở trạng thái `PENDING_PAY`.
*   **Main Flow:**
    1.  User chọn "Thanh toán bằng Ví".
    2.  Hệ thống kiểm tra số dư ví >= Tổng tiền Booking.
    3.  Hệ thống trừ tiền trong ví User.
    4.  Hệ thống cập nhật trạng thái Booking -> `CONFIRMED`.
    5.  Hệ thống sinh mã Check-in ngẫu nhiên.
    6.  Hệ thống tạo giao dịch (Transaction) lưu lịch sử.
*   **Alternative Flow (Không đủ tiền):**
    *   Hệ thống báo lỗi "Số dư không đủ". Gợi ý User nạp thêm tiền.

---

### UC11: Tạo sân mới (Create Venue)
*   **Actor:** Owner
*   **Main Flow:**
    1.  Owner nhập thông tin: Tên sân, Địa chỉ (Tỉnh/Huyện), Giờ mở/đóng cửa, Số điện thoại.
    2.  Hệ thống validate: Giờ mở < Giờ đóng.
    3.  Hệ thống lưu Venue vào DB với trạng thái `ACTIVE` (hoặc `PENDING` tùy cấu hình).
    4.  Hệ thống trả về thông tin sân vừa tạo.
