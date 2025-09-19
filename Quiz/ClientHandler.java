package Quiz;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private List<Question> questions;
    private Map<String, String> users;

    public ClientHandler(Socket socket, List<Question> questions, Map<String, String> users) {
        this.socket = socket;
        this.questions = questions;
        this.users = users;
    }

    @Override
    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            String action = null;
            String username = null;
            String password = null;

            // Đọc dữ liệu đăng nhập
            try {
                action = (String) ois.readObject();
                username = (String) ois.readObject();
                password = (String) ois.readObject();
                System.out.println("Received action: " + action + " for username: " + username);
            } catch (EOFException e) {
                System.out.println("⚠ Client " + socket.getInetAddress() + " disconnected during authentication.");
                return;
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Invalid data type received from " + socket.getInetAddress());
                return;
            }

            boolean success = false;
            String errorMessage = "";

            // Xử lý đăng nhập/đăng ký
            synchronized (users) {
                if ("register".equalsIgnoreCase(action)) {
                    if (username == null || username.trim().isEmpty()) {
                        errorMessage = "Tên đăng nhập không được để trống!";
                    } else if (username.trim().length() < 3 || username.trim().length() > 20) {
                        errorMessage = "Tên đăng nhập phải từ 3-20 ký tự!";
                    } else if (password == null || password.trim().isEmpty() || password.trim().length() < 3) {
                        errorMessage = "Mật khẩu phải có ít nhất 3 ký tự!";
                    } else if (users.containsKey(username.trim())) {
                        errorMessage = "Tên đăng nhập '" + username.trim() + "' đã được sử dụng!";
                    } else {
                        users.put(username.trim(), password.trim());
                        try (FileWriter fw = new FileWriter("src/users.txt", true)) {
                            fw.write(username.trim() + "," + password.trim() + "\n");
                        } catch (IOException e) {
                            System.err.println("❌ Failed to write to users.txt: " + e.getMessage());
                            errorMessage = "Lỗi lưu đăng ký!";
                        }
                        success = true;
                        System.out.println("🟢 User registered: " + username.trim());
                    }
                } else if ("login".equalsIgnoreCase(action)) {
                    if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                        errorMessage = "Tên đăng nhập hoặc mật khẩu không được để trống!";
                    } else if (!users.containsKey(username.trim()) || !users.get(username.trim()).equals(password.trim())) {
                        errorMessage = "Tài khoản hoặc mật khẩu không đúng!";
                    } else {
                        success = true;
                        System.out.println("🟢 User logged in: " + username.trim());
                    }
                }
            }

            // Gửi kết quả xác thực
            oos.writeBoolean(success);
            if (!success) {
                oos.writeObject(errorMessage);
                oos.flush();
                System.out.println("🔴 Authentication failed for " + username + ": " + errorMessage);
                return;
            }
            oos.flush();

            // Gửi câu hỏi
            oos.writeObject(questions);
            oos.flush();
            System.out.println("📤 Sent " + questions.size() + " questions to " + username);

            // Nhận đáp án
            @SuppressWarnings("unchecked")
            List<Integer> answers = (List<Integer>) ois.readObject();
            System.out.println("📥 Received " + answers.size() + " answers from " + username);

            int score = 0;
            for (int i = 0; i < answers.size(); i++) {
                if (answers.get(i) != null && answers.get(i) == questions.get(i).getCorrectAnswer()) {
                    score++;
                }
            }

            // Ghi kết quả
            try (FileWriter fw = new FileWriter("src/results.txt", true)) {
                fw.write("User " + username.trim() + " scored: " + score + "/" + questions.size() + "\n");
            } catch (IOException e) {
                System.err.println("❌ Failed to write to results.txt: " + e.getMessage());
            }

            oos.writeInt(score);
            oos.flush();
            System.out.println("✅ Client " + username.trim() + " scored " + score + " and disconnected successfully.");

        } catch (SocketException e) {
            System.out.println("⚠ Client " + (socket != null ? socket.getInetAddress() : "unknown") + " disconnected unexpectedly (TCP error): " + e.getMessage());
        } catch (IOException e) {
            System.err.println("❌ I/O error with client " + (socket != null ? socket.getInetAddress() : "unknown") + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Invalid object received from client " + (socket != null ? socket.getInetAddress() : "unknown") + ": " + e.getMessage());
        } finally {
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("🔚 Socket closed for client " + (socket.getInetAddress() != null ? socket.getInetAddress() : "unknown"));
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to close resources for client " + (socket != null ? socket.getInetAddress() : "unknown") + ": " + e.getMessage());
            }
        }
    }
}