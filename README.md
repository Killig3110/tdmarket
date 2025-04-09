# 🍇 TD Fruit Store - Android eCommerce App

Ứng dụng mua sắm trái cây trực tuyến, được phát triển bằng Java cho Android và sử dụng Firebase Firestore làm cơ sở dữ liệu.

---

## 🚀 Tính năng chính

- 👤 Đăng ký / Đăng nhập bằng Firebase Authentication
- 📦 Xem danh sách sản phẩm & chi tiết sản phẩm
- ❤️ Thêm / Xoá khỏi danh sách yêu thích (Wishlist)
- 🛒 Thêm sản phẩm vào giỏ hàng
- 🧾 Tạo đơn hàng và lưu chi tiết đơn hàng
- 💬 Bình luận, đánh giá sản phẩm (hỗ trợ trả lời bình luận)
- 🔐 Đổi mật khẩu qua email
- 👤 Trang cá nhân (avatar, thông tin người dùng)
- 🔄 Widget hiển thị sản phẩm ngẫu nhiên

---

## 🧱 Kiến trúc project

```
├── data/
│   ├── dao/                 # Các lớp DAO thao tác với Firestore
│   ├── entities/            # Các entity: Product, User, Order...
│   └── utils/               # Tiện ích FirestoreCallback, JsonReader,...
│
├── ui/
│   ├── adapter/             # RecyclerView Adapter
│   ├── activity/            # Màn hình chính (Home, Detail, Cart, Order, ...)
│   └── widget/              # Widget hiển thị sản phẩm
│
├── network/                 # ApiService, ApiClient (nếu dùng REST ngoài)
├── res/layout/              # XML layout cho các màn hình và dialog
└── MainActivity.java        # (nếu có)
```

---

## 🛠️ Công nghệ sử dụng

- ✅ Android SDK (Java)
- 🔥 Firebase Authentication
- 🔥 Firebase Firestore (Realtime NoSQL DB)
- 🖼 Glide (hiển thị ảnh)
- 🎯 SharedPreferences (lưu login info)
- 🔄 Custom FirestoreCallback để xử lý bất đồng bộ
- ✅ Parcelable/Serializable để truyền object giữa activity
- 🧠 Rating + Comment + Reply comment
- 🎨 UI Material + Dialogs + Animation

---

## ▶️ Cách chạy project

1. **Clone repo**:
   ```bash
   git clone https://github.com/your-username/td-fruit-store.git
   ```

2. **Mở bằng Android Studio**  
   (đã cấu hình `compileSdk = 33`)

3. **Tạo Firebase Project**  
   Kết nối Firebase + bật Firestore và Authentication (Email/Password)

4. **Chạy app trên thiết bị hoặc emulator**

---

## 👨‍💻 Tác giả

- Tên: [Huynh Thanh Duy]
