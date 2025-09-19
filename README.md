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
  <img src="docs/chat_mess.png" alt="Server UI" width="700"/>
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
- **Hệ điều hành**: Windows / macOS / Linux  
- **IDE khuyến nghị**: IntelliJ IDEA, Eclipse, hoặc NetBeans  
- **Bộ nhớ**: Tối thiểu 512MB RAM  

---

### 📦 Các bước triển khai

#### 🔹 Bước 1: Chuẩn bị môi trường
1. **Cài đặt JDK** nếu chưa có:  
   - Kiểm tra bằng lệnh:  
     ```bash
     java -version
     javac -version
     ```
   - Nếu chưa có, tải JDK tại [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) hoặc [OpenJDK](https://adoptium.net/).

2. **Tải mã nguồn dự án**:  
   - Clone repo bằng Git:  
     ```bash
     git clone https://github.com/mthanh04/LTM-ChatRoom
     ```
   - Hoặc tải file `.zip` và giải nén.

---

#### 🔹 Bước 2: Biên dịch mã nguồn
Di chuyển đến thư mục `bin` rồi biên dịch:  
```bash
cd BOXCHAT/bin
rmiregistry
```
#### 🔹 Bước 3: Chạy file ChatServer.java

#### 🔹 Bước 4: Chạy file ChatClient.java
- Giao diện chat sẽ hiện ra

## 👤 5. Liên hệ
**Họ tên**: Trịnh Minh Thành.  
**Lớp**: CNTT 16-03.  
**Email**: thanhmeo260604@gmail.com.

© 2025 Faculty of Information Technology, DaiNam University. All rights reserved.



