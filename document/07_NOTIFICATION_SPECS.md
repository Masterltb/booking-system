# 07_NOTIFICATION_SPECS.md

## 1. Các loại Thông báo (Notification Types)

| Type | Trigger Event | Title Template | Message Template | Target |
| :--- | :--- | :--- | :--- | :--- |
| `BOOKING_SUCCESS` | Thanh toán thành công | Đặt sân thành công! | Bạn đã đặt {court_name} vào lúc {time}. Mã check-in: {code}. | User |
| `BOOKING_REMINDER` | Trước giờ đá 1 tiếng | Sắp đến giờ ra sân! | Đừng quên lịch đặt sân tại {venue} lúc {time} nhé. | User |
| `BOOKING_CANCELLED` | Hủy sân (User/System) | Lịch đặt sân đã bị hủy | Lịch đặt {court_name} lúc {time} đã bị hủy. Tiền hoàn: {amount}. | User |
| `NEW_BOOKING` | Có khách đặt sân | Có đơn đặt sân mới | Khách {user_name} vừa đặt {court_name} lúc {time}. | Owner |
| `WALLET_DEPOSIT` | Nạp tiền thành công | Nạp tiền thành công | Bạn vừa nạp {amount} vào ví. Số dư hiện tại: {balance}. | User |
| `SETTLEMENT` | Đối soát doanh thu | Tiền về ví! | Hệ thống đã cộng {amount} doanh thu từ booking #{id}. | Owner |

## 2. Cơ chế Gửi (Delivery Mechanism)

### 2.1. In-App Notification (Lưu DB)
- Mọi thông báo đều được lưu vào bảng `notifications`.
- User mở App/Web sẽ thấy chuông thông báo có chấm đỏ.
- API: `GET /api/v1/notifications` (Polling hoặc load khi mở app).

### 2.2. Push Notification (Mobile App - Future)
- Sử dụng **Firebase Cloud Messaging (FCM)**.
- Khi Backend tạo notification -> Gửi async request tới FCM Server kèm `device_token` của user.

### 2.3. Email (Quan trọng)
- Gửi email xác nhận khi: Đăng ký, Quên mật khẩu, Đặt sân thành công.
- Sử dụng: **JavaMailSender** + **Thymeleaf Template**.
