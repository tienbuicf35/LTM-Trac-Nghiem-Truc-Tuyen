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
        <img src="images/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="images/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="images/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20T…he-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
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

## 🚀 3. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)  
## 4. Tính năng chính
- Quản lý danh sách câu hỏi/đáp án.
- Cho phép nhiều người dùng thi cùng lúc.
- Tính điểm tự động và gửi kết quả ngay sau khi nộp bài.
- Lưu trữ kết quả vào file.
- Giao diện trực quan bằng Swing.

---
