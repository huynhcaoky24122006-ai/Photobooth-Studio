# 📸 Photobooth Studio - Smart Mobile Application

> Một ứng dụng chụp ảnh Photobooth thông minh trên di động tích hợp nhận diện khuôn mặt, chỉnh sửa ảnh trực quan, lưu trữ kho ảnh cá nhân và quản lý tài nguyên động từ Admin.

---

## 📌 Tổng Quan Dự Án

**Photobooth Studio** là giải pháp phần mềm toàn diện tái hiện trải nghiệm chụp ảnh photobooth thực tế ngay trên thiết bị di động. Hệ thống không chỉ hỗ trợ các luồng chụp ảnh, ghép khung, dán sticker cơ bản mà còn giải quyết bài toán vận hành thực tế thông qua cơ chế phân quyền tài khoản, mã QR tải ảnh bảo mật có thời hạn và hệ thống quản trị nội dung linh hoạt (CMS).

### 👥 Nhóm Người Dùng Chính:
- **Guest (Khách):** Chụp ảnh theo layout, tùy chỉnh bộ lọc/sticker, xuất ảnh và quét QR code để tải ảnh tạm thời.
- **Member (Thành viên):** Đăng nhập, lưu trữ kho ảnh cá nhân, tải lại/in lại ảnh, đánh dấu yêu thích và sử dụng các tài nguyên đặc quyền (VIP).
- **Admin (Quản trị viên):** Quản lý tài nguyên thiết kế (Frame/Sticker), phân quyền VIP, quản lý người dùng và theo dõi biểu đồ thống kê hệ thống.

---

## 🔥 Tính Năng Nổi Bật (Key Features)

### 1. 🤖 Photobooth Thông Minh với AI/Computer Vision
- **Phát hiện khuôn mặt thời gian thực:** Nhận diện vị trí khuôn mặt trực tiếp trên camera preview.
- **Căn chỉnh tự động & Gợi ý góc chụp:** Đưa ra các chỉ dẫn thông minh *(“Dịch sang trái”, “Đưa mặt vào khung”)* dựa trên layout đã chọn và tự động crop/căn giữa khuôn mặt tối ưu.

### 2. 🔒 Tải Ảnh Bằng Mã QR Bảo Mật Có Thời Hạn
- **Quyền riêng tư tuyệt đối:** Ảnh của Guest được lưu trữ dưới dạng tạm thời.
- **Token mã hóa & Tự động hết hạn:** Sinh mã QR kèm Token giới hạn thời gian (10-30 phút). Sau khi hết hạn, liên kết bị hủy và hệ thống tự động dọn dẹp bộ nhớ.

### 3. 🎨 Hệ Thống Frame & Sticker Động Theo Sự Kiện
- **Cập nhật Real-time từ Server:** Admin có thể tải lên các Frame/Sticker mới theo sự kiện (Tết, Noel, Sinh nhật, Halloween...) mà **không cần phát hành lại ứng dụng**.
- **Cơ chế Phân Cấp Dịch Vụ (VIP System):** Tài nguyên được phân loại `FREE` hoặc `VIP`. Member VIP có thể mở khóa các mẫu khung hình và sticker độc quyền.

---

## 🚀 Lộ Trình Phát Triển (Development Roadmap)

- [x] **Giai đoạn 1: Nền tảng & Chụp ảnh cơ bản (MVP)**
  - Luồng Camera preview, chọn layout, đếm ngược 3s, ghép ảnh theo grid layout.
- [x] **Giai đoạn 2: Bộ Chỉnh Sửa Ảnh Trực Quan**
  - Tích hợp bộ lọc màu (Filter), chèn Frame PNG, thao tác Sticker (Kéo, thả, phóng to, xoay).
- [x] **Giai đoạn 3: Xuất Ảnh, Tạo QR Code & In Ảnh**
  - Xử lý lưu ảnh, sinh QR code tải ảnh có hiệu lực theo thời gian và tích hợp luồng in.
- [ ] **Giai đoạn 4: Quản Lý Tài Khoản Member & Kho Ảnh**
  - Đăng ký/Đăng nhập, quản lý Profile, Sync kho ảnh cá nhân.
- [ ] **Giai đoạn 5: Hệ Thống VIP & Admin CMS**
  - Dashboard Admin upload/quản lý tài nguyên động, cấu hình gói VIP.
- [ ] **Giai đoạn 6: Quản Lý Người Dùng & Thống Kê**
  - Biểu đồ thống kê số lượng ảnh chụp, quản lý/khóa tài khoản vi phạm.
- [ ] **Giai đoạn 7: Testing, Optimization & Demo**
  - Tối ưu hóa bộ nhớ Bitmap, Cache tài nguyên và kiểm thử bảo mật.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### **Mobile App (Frontend)**
- **Language:** Java / Kotlin (Android Studio)
- **Camera API:** CameraX / Camera2 API
- **Computer Vision:** Google ML Kit / OpenCV (Face Detection)
- **UI/UX:** XML Layouts, Custom Views, Glide/Picasso (Image Loading)

### **Backend & Database**
- **Cloud/Backend:** Node.js / Firebase (Authentication, Realtime Database, Cloud Storage)
- **Storage:** Firebase Storage / Amazon S3 (Lưu trữ ảnh & asset PNG)

---

## 📸 Demo & Giao Diện Ứng Dụng

| Màn hình Chụp ảnh | Bộ chỉnh sửa Sticker | Màn hình QR Code |
| :---: | :---: | :---: |
| *(Thêm hình ảnh/GIF demo ở đây)* | *(Thêm hình ảnh/GIF demo ở đây)* | *(Thêm hình ảnh/GIF demo ở đây)* |

---

## ⚙️ Cài Đặt & Khởi Chạy (Installation)

### **Yêu cầu hệ thống:**
* Android Studio Jellyfish (hoặc mới hơn)
* JDK 17 / Android SDK version 24+ (Android 7.0 trở lên)
* Đã cấu hình file `google-services.json` (nếu dùng Firebase)

### **Các bước thực hiện:**
1. Clone repository về máy:
   ```bash
   git clone [https://github.com/username/Photobooth-Studio.git](https://github.com/username/Photobooth-Studio.git)
