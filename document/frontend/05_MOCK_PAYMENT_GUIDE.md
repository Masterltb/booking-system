# 05_MOCK_PAYMENT_GUIDE.md

Hướng dẫn dựng trang giả lập thanh toán (Mock Payment Gateway) trên Frontend.

## 1. Mục đích
Vì chưa có tài khoản Merchant thật của VNPay, chúng ta sẽ tự dựng một trang "fake" để test luồng thanh toán.

## 2. Luồng hoạt động
1.  User đặt sân -> Backend trả về URL: `http://localhost:5173/mock-payment?bookingId=101&amount=150000`.
2.  Frontend (React) nhận URL này và redirect user đến đó.
3.  Trang `/mock-payment` hiện ra giao diện giống VNPay.
4.  User bấm nút "Thanh toán".
5.  Frontend gọi API Backend để xác nhận.

## 3. Triển khai (React Code)

Tạo file `src/features/payment/pages/MockPaymentPage.tsx`:

```typescript
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios'; // Dùng axios instance đã config

export const MockPaymentPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const bookingId = searchParams.get('bookingId');
  const amount = searchParams.get('amount');
  const [status, setStatus] = useState<'idle' | 'processing' | 'success' | 'error'>('idle');

  const handlePayment = async (responseCode: string) => {
    setStatus('processing');
    try {
      // Gọi API IPN của Backend
      const res = await axios.get(`/payments/ipn`, {
        params: {
          vnp_ResponseCode: responseCode,
          vnp_TxnRef: bookingId
        }
      });

      if (res.data.RspCode === '00') {
        setStatus('success');
        setTimeout(() => navigate('/bookings'), 2000); // Redirect về lịch sử đặt sân
      } else {
        setStatus('error');
      }
    } catch (error) {
      setStatus('error');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-96 text-center">
        <h2 className="text-2xl font-bold text-blue-600 mb-4">Cổng Thanh Toán Giả Lập</h2>
        
        <div className="mb-6">
          <p className="text-gray-600">Đơn hàng #</p>
          <p className="text-xl font-bold">{bookingId}</p>
        </div>

        <div className="mb-8">
          <p className="text-gray-600">Số tiền</p>
          <p className="text-3xl font-bold text-green-600">
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(amount))}
          </p>
        </div>

        {status === 'idle' && (
          <div className="space-y-3">
            <button
              onClick={() => handlePayment('00')}
              className="w-full bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded transition"
            >
              Thanh toán Thành công
            </button>
            <button
              onClick={() => handlePayment('99')}
              className="w-full bg-red-500 hover:bg-red-600 text-white py-2 px-4 rounded transition"
            >
              Hủy giao dịch
            </button>
          </div>
        )}

        {status === 'processing' && <p>Đang xử lý...</p>}
        {status === 'success' && <p className="text-green-600 font-bold">Thanh toán thành công!</p>}
        {status === 'error' && <p className="text-red-600 font-bold">Giao dịch thất bại.</p>}
      </div>
    </div>
  );
};
```

## 4. Cấu hình Route
Thêm route này vào `App.tsx` hoặc file route config:

```typescript
<Route path="/mock-payment" element={<MockPaymentPage />} />
```
