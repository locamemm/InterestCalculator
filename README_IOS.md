# Hướng Dẫn Phiên Bản iOS - Trợ Lý Ngân Hàng (InterestCalculator)

Phiên bản iOS được phát triển bằng **Swift / SwiftUI** với cấu trúc và tính năng tương đồng 100% so với phiên bản Android (Jetpack Compose). 

---

## 📱 Tính Năng Trên Phiên Bản iOS

1. **Header Banner Quản Lý KPI**:
   - Hiển thị thông tin Chuyên viên tín dụng & Ngân hàng.
   - Thanh tiến độ giải ngân cho vay & huy động tiết kiệm thực tế so với chỉ tiêu.
   - Chuông thông báo số lượng sổ tiết kiệm sắp đến hạn (trong vòng 15 ngày).
   - Dialog chỉnh sửa thông tin & chỉ tiêu KPI cá nhân.

2. **Tab 1: CHO VAY (Loan Tab)**:
   - **Công cụ tính toán khoản vay**:
     - Nhập số tiền vay với gợi ý nhanh (100tr, 500tr, 1 tỷ, 2 tỷ, 5 tỷ) và đọc số thành chữ chuẩn tiếng Việt.
     - Kỳ hạn vay (tháng/năm), kỳ hạn trả nợ (hàng tháng, 3 tháng, 6 tháng, 12 tháng).
     - Lãi suất vay theo năm (%/năm) hoặc theo tháng (%/tháng).
     - Lãi suất ưu đãi thời gian đầu và thời gian ân hạn gốc (chỉ trả lãi).
     - 4 phương thức tính: Dư nợ giảm dần, Trả góp đều (Niên kim), Lãi phẳng, Lãi cuối kỳ.
     - Bảng tóm tắt dòng tiền trả nợ: Tiền trả kỳ đầu, kỳ cuối, kỳ cao nhất, tổng lãi, tổng thanh toán.
     - **Xem lịch trả nợ chi tiết**: Bảng phân bổ gốc, lãi, số tiền trả và dư nợ từng kỳ.
     - **Soạn tin nhắn tư vấn**: Tạo nội dung tư vấn chuyên nghiệp gửi Zalo/SMS chỉ với 1 chạm.
     - **Lưu thông số mẫu (Preset)** và **Lưu vào danh sách hồ sơ khách hàng**.
   - **Danh sách hồ sơ vay**:
     - Tìm kiếm theo tên, số điện thoại, mục đích vay.
     - Lọc theo trạng thái: Đang tư vấn, Đã duyệt, Đã giải ngân, Đã tất toán.
     - Gọi điện hoặc nhắn tin trực tiếp cho khách hàng.
     - Xem lại lịch trả nợ, bảng tư vấn, chỉnh sửa hoặc xóa hồ sơ.

3. **Tab 2: GỬI TIẾT KIỆM (Savings Tab)**:
   - **Công cụ tính toán tiền gửi tiết kiệm**:
     - Số tiền gửi, kỳ hạn gửi (1 - 36 tháng).
     - Lãi suất huy động (%/năm).
     - Hình thức nhận lãi: Lãi cuối kỳ, Lãi hàng tháng, Lãi hàng quý, Lãi đầu kỳ.
     - Tự động quay vòng lãi kép (nhập lãi vào gốc).
     - Bảng tóm tắt: Tổng lãi thực nhận, lãi suất thực hưởng (%/năm), tổng nhận khi đáo hạn.
     - Xem lịch nhận lãi chi tiết theo từng kỳ.
     - Soạn tin nhắn tư vấn gửi tiết kiệm Zalo/SMS.
   - **Danh sách sổ tiết kiệm**:
     - Quản lý danh mục sổ của khách hàng.
     - Đếm ngược số ngày đến hạn đáo hạn, cảnh báo sổ sắp đến hạn màu cam nổi bật.
     - Thao tác gọi điện tư vấn tái tục sổ nhanh chóng.

4. **Tab 3: TÍNH DƯ NỢ ĐA NGUỒN (Debt Consolidation)**:
   - Thêm nhiều khoản nợ từ nhiều nguồn: Khối Ngân hàng và Khối Tổ chức tín dụng (FE Credit, Home Credit, thẻ tín dụng...).
   - Lãi suất chung từng khối hoặc cấu hình lãi suất riêng cho từng khoản nợ.
   - Tính toán lãi suất bình quân gia quyền của toàn bộ danh mục nợ.
   - Lập lịch trả nợ tổng hợp và xem chi tiết phân bổ từng khoản nợ theo từng tháng.
   - Soạn tin nhắn tư vấn gom nợ, tái cấu trúc tài chính gửi khách hàng.

---

## 🚀 Cách Build File `.ipa` Bằng GitHub Actions

Dự án đã được tích hợp sẵn Workflow GitHub Actions tại file [`.github/workflows/build-ios.yml`](.github/workflows/build-ios.yml).

### Các bước thực hiện:
1. Đẩy mã nguồn lên kho lưu trữ GitHub:
   ```bash
   git add .
   git commit -m "Add complete iOS version and GitHub Actions build workflow"
   git push origin main
   ```
2. Mở repository trên trình duyệt GitHub -> chuyển đến tab **Actions**.
3. Chọn workflow **Build iOS IPA** ở cột bên trái.
4. Nhấn nút **Run workflow** -> Chọn branch `main` và cấu hình `Release` -> Nhấn **Run workflow**.
5. GitHub Actions sẽ tự động:
   - Khởi động máy ảo macOS (Apple Silicon).
   - Chạy toàn bộ Unit Tests kiểm tra tính toán tài chính (`FinancialCalculationsTests`).
   - Biên dịch ứng dụng và đóng gói file `InterestCalculator.ipa`.
   - Tạo mã băm SHA256 để kiểm tra tính toàn vẹn.
   - Tải file `InterestCalculator.ipa` lên mục **Artifacts** của lượt chạy.
6. Sau khi build xong (khoảng 2-3 phút), nhấn vào lượt chạy (Run) và tải file **`InterestCalculator-iOS-IPA`** ở mục **Artifacts** về máy.

---

## 📲 Cách Cài Đặt File `.ipa` Lên iPhone / iPad

Sau khi tải file `.ipa` từ GitHub Actions về máy tính hoặc điện thoại:

### Cách 1: Sử dụng Sideloadly (Khuyên dùng - Windows & Mac)
1. Tải và cài đặt [Sideloadly](https://sideloadly.io/) trên máy tính Windows.
2. Kết nối iPhone với máy tính bằng cáp sạc (chọn "Tin cậy máy tính này" trên iPhone).
3. Kéo thả file `InterestCalculator.ipa` vào giao diện Sideloadly.
4. Nhập Apple ID của bạn và nhấn **Start**.
5. Sau khi cài xong, trên iPhone vào: **Cài đặt > Cài đặt chung > Quản lý VPN & Thiết bị** > chọn Apple ID của bạn > nhấn **Tin cậy**.

### Cách 2: Sử dụng AltStore
- Cài đặt AltServer trên máy tính và AltStore trên iPhone.
- Mở AltStore trên iPhone > tab "My Apps" > nhấn dấu `+` > chọn file `InterestCalculator.ipa`.

### Cách 3: Sử dụng TrollStore (Nếu iOS hỗ trợ)
- Dành cho các thiết bị chạy iOS 14.0 - 16.6.1 hoặc 17.0: Mở trực tiếp file `.ipa` trong TrollStore để cài vĩnh viễn không cần ký lại.

