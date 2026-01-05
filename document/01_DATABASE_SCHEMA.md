# 01_DATABASE_SCHEMA.md

## 1. Module Identity

### Table `users`
- **id**: Long (PK, Auto Increment)
- **email**: String (Unique, Index)
- **password_hash**: String
- **full_name**: String
- **phone**: String
- **avatar_url**: String (Nullable)
- **role**: ENUM (`ADMIN`, `OWNER`, `USER`)
- **status**: ENUM (`ACTIVE`, `BANNED`)
- **created_at**: Timestamp
- **updated_at**: Timestamp

### Table `refresh_tokens`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **token**: String (Unique, Index)
- **expiry_date**: Timestamp
- **created_at**: Timestamp

---

## 2. Module Subscription (Quản lý gói cước Chủ sân)

### Table `subscription_plans`
- **id**: Long (PK)
- **name**: String
- **price**: Decimal
- **duration_days**: Integer
- **max_venues**: Integer
- **description**: Text
- **is_active**: Boolean

### Table `user_subscriptions`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **plan_id**: Long (FK -> `subscription_plans.id`)
- **start_date**: Timestamp
- **end_date**: Timestamp
- **status**: ENUM (`ACTIVE`, `EXPIRED`, `CANCELLED`)
- **payment_ref**: String

---

## 3. Module Venue (Quản lý Sân bãi)

### Table `venues` (Cụm sân)
- **id**: Long (PK)
- **owner_id**: Long (FK -> `users.id`)
- **name**: String
- **description**: Text
- **address**: String
- **province**: String
- **district**: String
- **ward**: String
- **latitude**: Double
- **longitude**: Double
- **open_time**: Time
- **close_time**: Time
- **phone_contact**: String
- **status**: ENUM (`ACTIVE`, `INACTIVE`, `PENDING_APPROVAL`)
- **average_rating**: Double (Default 0.0 - Mới thêm)
- **total_reviews**: Integer (Default 0 - Mới thêm)

### Table `venue_images`
- **id**: Long (PK)
- **venue_id**: Long (FK -> `venues.id`)
- **image_url**: String
- **is_thumbnail**: Boolean

### Table `courts` (Sân con)
- **id**: Long (PK)
- **venue_id**: Long (FK -> `venues.id`)
- **name**: String
- **description**: String
- **status**: ENUM (`ACTIVE`, `MAINTENANCE`)

### Table `pricing_rules` (Cấu hình giá linh động)
- **id**: Long (PK)
- **venue_id**: Long (FK -> `venues.id`)
- **court_id**: Long (Nullable)
- **start_time**: Time
- **end_time**: Time
- **price_per_hour**: Decimal
- **priority**: Integer
- **days_of_week**: String

---

## 4. Module Booking (Đặt sân)

### Table `bookings`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **court_id**: Long (FK -> `courts.id`)
- **start_time**: Timestamp (Index)
- **end_time**: Timestamp (Index)
- **total_price**: Decimal
- **platform_fee**: Decimal
- **status**: ENUM (`PENDING_PAY`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `CHECKED_IN`)
- **payment_method**: ENUM (`VNPAY`, `WALLET`, `CASH`)
- **payment_ref**: String
- **checkin_code**: String
- **is_settled**: Boolean
- **created_at**: Timestamp

---

## 5. Module Wallet (Ví điện tử nội bộ)

### Table `wallets`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **balance**: Decimal
- **currency**: String
- **updated_at**: Timestamp

### Table `transactions`
- **id**: Long (PK)
- **wallet_id**: Long (FK -> `wallets.id`)
- **amount**: Decimal
- **type**: ENUM (`DEPOSIT`, `WITHDRAW`, `BOOKING_PAYMENT`, `BOOKING_REFUND`, `SETTLEMENT`, `SUBSCRIPTION_PAYMENT`)
- **status**: ENUM (`PENDING`, `SUCCESS`, `FAILED`)
- **description**: String
- **ref_id**: Long
- **created_at**: Timestamp

---

## 6. Module Social & Notification

### Table `reviews`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **venue_id**: Long (FK -> `venues.id`)
- **booking_id**: Long (FK -> `bookings.id`)
- **rating**: Integer (1-5)
- **comment**: Text
- **created_at**: Timestamp

### Table `notifications`
- **id**: Long (PK)
- **user_id**: Long (FK -> `users.id`)
- **title**: String
- **message**: String
- **type**: ENUM (`BOOKING`, `SYSTEM`, `PROMOTION`)
- **is_read**: Boolean
- **created_at**: Timestamp
