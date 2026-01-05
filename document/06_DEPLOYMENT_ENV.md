# 06_DEPLOYMENT_ENV.md

## 1. Environment Variables (.env)

### Database Config
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=badminton_db
DB_USER=root
DB_PASSWORD=secret
```

### JWT Security
```properties
JWT_SECRET=7a91b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z
JWT_EXPIRATION_MS=3600000  # 1 hour
REFRESH_TOKEN_EXPIRATION_MS=1209600000 # 14 days
```

### Payment Gateway (VNPay Sandbox)
```properties
VNP_TMN_CODE=YOUR_TMN_CODE
VNP_HASH_SECRET=YOUR_HASH_SECRET
VNP_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNP_RETURN_URL=http://localhost:3000/payment-result
```

### Redis Cache
```properties
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 2. Docker Compose (docker-compose.yml)

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: badminton_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"

  backend:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    env_file:
      - .env

volumes:
  mysql_data:
```
