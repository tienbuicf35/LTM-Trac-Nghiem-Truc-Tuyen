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
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            String action = (String) ois.readObject(); // login hoặc register
            String username = (String) ois.readObject();
            String password = (String) ois.readObject();

            boolean success = false;
            String errorMessage = "";

            synchronized (users) {
                if ("register".equalsIgnoreCase(action)) {
                    // Kiểm tra tên đăng nhập hợp lệ
                    if (username == null || username.trim().isEmpty()) {
                        errorMessage = "Tên đăng nhập không được để trống!";
                    } else if (username.trim().length() < 3) {
                        errorMessage = "Tên đăng nhập phải có ít nhất 3 ký tự!";
                    } else if (username.trim().length() > 20) {
                        errorMessage = "Tên đăng nhập không được quá 20 ký tự!";
                    } else if (password == null || password.trim().isEmpty()) {
                        errorMessage = "Mật khẩu không được để trống!";
                    } else if (password.trim().length() < 3) {
                        errorMessage = "Mật khẩu phải có ít nhất 3 ký tự!";
                    } else if (users.containsKey(username.trim())) {
                        errorMessage = "Tên đăng nhập '" + username.trim() + "' đã được sử dụng!\nVui lòng chọn tên khác.";
                    } else {
                        // Đăng ký thành công
                        String cleanUsername = username.trim();
                        String cleanPassword = password.trim();
                        users.put(cleanUsername, cleanPassword);
                        try (FileWriter fw = new FileWriter("src/users.txt", true)) {
                            fw.write(cleanUsername + "," + cleanPassword + "\n");
                        }
                        success = true;
                        System.out.println("🟢 User registered: " + cleanUsername);
                    }
                } else if ("login".equalsIgnoreCase(action)) {
                    if (username == null || username.trim().isEmpty()) {
                        errorMessage = "Tên đăng nhập không được để trống!";
                    } else if (password == null || password.trim().isEmpty()) {
                        errorMessage = "Mật khẩu không được để trống!";
                    } else if (!users.containsKey(username.trim())) {
                        errorMessage = "Tài khoản '" + username.trim() + "' không tồn tại!\nVui lòng đăng ký trước khi đăng nhập.";
                    } else if (!users.get(username.trim()).equals(password.trim())) {
                        errorMessage = "Mật khẩu không đúng!\nVui lòng kiểm tra lại.";
                    } else {
                        success = true;
                        System.out.println("🟢 User logged in: " + username.trim());
                    }
                }
            }

            oos.writeBoolean(success);
            if (!success) {
                oos.writeObject(errorMessage);
            }
            oos.flush();

            if (!success) {
                socket.close();
                return;
            }

            // gửi câu hỏi
            oos.writeObject(questions);
            oos.flush();
            System.out.println("Đã gửi " + questions.size() + " câu hỏi cho client " + username.trim());

            // nhận đáp án
            @SuppressWarnings("unchecked")
            List<Integer> answers = (List<Integer>) ois.readObject();

            int score = 0;
            for (int i = 0; i < answers.size(); i++) {
                if (answers.get(i) == questions.get(i).getCorrectAnswer()) {
                    score++;
                }
            }

            try (FileWriter fw = new FileWriter("src/results.txt", true)) {
                fw.write("User " + username.trim() + " scored: " + score + "/" + questions.size() + "\n");
            }

            oos.writeInt(score);
            oos.flush();

            oos.close();
            ois.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
