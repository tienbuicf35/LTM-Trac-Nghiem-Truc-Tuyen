<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG TRẮC NGHIỆM TRỰC TUYẾN
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu
Ứng dụng **Trắc nghiệm trực tuyến** sử dụng giao thức **TCP** cho phép nhiều người dùng đăng ký, đăng nhập và làm bài thi trắc nghiệm trực tiếp qua mạng.  

- **Server**: đóng vai trò trung tâm, quản lý kết nối, phân phát câu hỏi, nhận kết quả và lưu trữ lịch sử làm bài.  
- **Client**: cung cấp giao diện người dùng để đăng ký, đăng nhập, làm bài trắc nghiệm và nhận kết quả.  
- **Lưu trữ dữ liệu**: thông tin tài khoản, câu hỏi và kết quả làm bài được lưu vào file văn bản để triển khai đơn giản.  

### ⚙️ Các chức năng chính  

🖥 **Server**  
- 🔗 Kết nối & Quản lý client.  
- 👤 Quản lý người dùng: đăng ký, đăng nhập, kiểm tra trùng tài khoản.  
- ❓ Quản lý câu hỏi: nạp câu hỏi từ file, gửi cho client.  
- 📊 Chấm điểm & Lưu kết quả: tự động so sánh đáp án, ghi điểm số vào file.  
- 🗂 Quản lý dữ liệu: danh sách tài khoản, lịch sử kết quả.  

💻 **Client**  
- 🔗 Kết nối Server qua TCP.  
- 👤 Đăng ký & Đăng nhập tài khoản.  
- ❓ Làm bài trắc nghiệm: hiển thị câu hỏi, chọn đáp án.  
- ⏱ Quản lý thời gian làm bài: đếm ngược, tự động nộp khi hết giờ.  
- 📊 Nhận kết quả: hiển thị điểm số sau khi nộp bài.  

---

## 2. Công nghệ sử dụng
- **Java Core**
- **Java Swing**: xây dựng giao diện người dùng
- **Java Sockets (TCP)**: `ServerSocket`, `Socket`
- **Multithreading**: hỗ trợ nhiều client làm bài cùng lúc
- **File I/O**: lưu trữ danh sách câu hỏi, kết quả làm bài

### Các thư viện/Package chính:
- `java.net.Socket`, `java.net.ServerSocket`
- `java.io.*` (PrintWriter, BufferedReader,…)
- `java.util.ArrayList`, `java.util.HashMap`
- `javax.swing.*`

---

## 🚀 3. Hình ảnh các chức năng

<p align="center">
  <img width="542" height="421" alt="image" src="https://github.com/user-attachments/assets/e3631519-20a4-45e4-bcb4-ec71c91ce74c" />

</p>
<p align="center">
  <em>Hình 1: Giao diện chat chính của các client</em>
</p>

<p align="center">
  <img src="docs/mess.png" alt="Client UI" width="500"/>
</p>
<p align="center">
  <em>Hình 2: Giao diện Client chat nhóm</em>
</p>

<p align="center">
  <img src="docs/mess.png" alt="Message Broadcast" width="700"/>
</p>
<p align="center">
  <em>Hình 3: Thông báo khi có client rời khỏi nhóm chat</em>
</p>

---

## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống

- **Java Development Kit (JDK)**: Phiên bản 8 trở lên
- **Hệ điều hành**: Windows, macOS, hoặc Linux
- **Môi trường phát triển**: IDE (IntelliJ IDEA, Eclipse, VS Code) hoặc terminal/command prompt
- **Bộ nhớ**: Tối thiểu 512MB RAM
- **Dung lượng**: Khoảng 10MB cho mã nguồn và file thực thi

### 📦 Cài đặt và triển khai

#### Bước 1: Chuẩn bị môi trường
1. **Kiểm tra Java**: Mở terminal/command prompt và chạy:
   ```bash
   java -version
   javac -version
   ```
   Đảm bảo cả hai lệnh đều hiển thị phiên bản Java 8 trở lên.

2. **Tải mã nguồn**: Sao chép thư mục `UngDungChat_TCP` chứa các file:
   - `Server.java`
   - `Client.java`

#### Bước 2: Biên dịch mã nguồn
1. **Mở terminal** và điều hướng đến thư mục chứa mã nguồn
2. **Biên dịch các file Java**:
   ```bash
   javac Quiz/*.java
   ```
   Hoặc biên dịch từng file riêng lẻ:
   ```bash
   javac Quiz/Server.java
   javac Quiz/Client.java
   ```

3. **Kiểm tra kết quả**: Nếu biên dịch thành công, sẽ tạo ra các file `.class` tương ứng.

#### Bước 3: Chạy ứng dụng

**Khởi động Server:**
```bash
java Quiz.Server
```
- Server sẽ khởi động trên port mặc định (5555)
- Giao diện server sẽ hiển thị, sẵn sàng nhận kết nối từ client
- Server sẽ tạo file `client_results.txt` để lưu lịch sử làm bài

**Khởi động Client:**
```bash
java Quiz.Client
```
- Mở terminal mới cho mỗi client
- Nhập tên và mật khẩu người dùng khi được yêu cầu
- Client sẽ kết nối đến server và hiển thị giao diện chat

### 🚀 Sử dụng ứng dụng

1. **Kết nối**: Client tự động kết nối đến server sau khi nhập tên đăng nhập và mật khẩu
2. **Gửi tin nhắn**: Chọn đáp án cho từng câu hỏi và nhấn nút "Câu tiếp theo" hoặc "Nộp bài"
3. **Nhận tin nhắn**: Kết quả bài thi sẽ hiển thị sau khi nộp bài, bao gồm điểm số và xếp loại
4. **Lịch sử chat**: Server tự động lưu kết quả bài thi vào file `client_results.txt`
5. **Ngắt kết nối**: Đóng cửa sổ client hoặc nhấn thoát để thoát chương trình


## Thông tin liên hệ  
Họ tên: Bùi Văn Tiến.  
Lớp: CNTT 16-03.  
Email: tienbuicf35@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
