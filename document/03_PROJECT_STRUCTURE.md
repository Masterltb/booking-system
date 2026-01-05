# 03_PROJECT_STRUCTURE.md

```
src/main/java/com/badminton/booking
├── BadmintonBookingApplication.java
│
├── common                      # DÙNG CHUNG (KHÔNG CHỨA LOGIC NGHIỆP VỤ CỤ THỂ)
│   ├── config                  # Security, Swagger, Redis Config
│   ├── exception               # GlobalExceptionHandler
│   ├── model                   # ApiResponse, PagedResponse
│   └── utils                   # DateUtils, SecurityUtils
│
├── modules                     # CÁC MODULE NGHIỆP VỤ (ĐỘC LẬP TỐI ĐA)
│   ├── identity                # Quản lý User, Auth, Wallet cơ bản
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto                 # Dùng Java Record (Java 21)
│   │
│   ├── venue                   # Quản lý Sân, Giá
│   │   ├── ...
│   │
│   ├── booking                 # Quản lý Đặt sân, Thanh toán
│   │   ├── ...
│   │
│   └── notification            # Quản lý Chat, Thông báo
│       ├── websocket           # Các class xử lý Socket
│       └── ...
```

## Quy tắc Code (Coding Convention)

1. **DTO:** Bắt buộc dùng `public record NameDTO(...) {}`.
2. **Service:** Phải có Interface (`IBookingService`) và Implementation (`BookingServiceImpl`).
3. **Dependency Injection:** Dùng Constructor Injection (Annotation `@RequiredArgsConstructor` của Lombok). Không dùng `@Autowired` trên field.
4. **Mapping:** Dùng thư viện **MapStruct** để convert Entity <-> DTO (Không code tay getter/setter để copy).
