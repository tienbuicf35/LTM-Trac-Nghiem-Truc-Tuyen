package Quiz;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                String command = (String) ois.readObject();
                String username = (String) ois.readObject();
                String password = (String) ois.readObject();

                boolean success = false;
                String message = "";

                if ("register".equals(command)) {
                    if (Quizserver.users.containsKey(username)) {
                        message = "⚠ Tài khoản đã tồn tại!";
                    } else {
                        Quizserver.users.put(username, password);
                        Quizserver.saveUsers("src/users.txt");
                        success = true;
                        message = "✅ Đăng ký thành công!";
                    }
                } else if ("login".equals(command)) {
                    if (Quizserver.users.containsKey(username) &&
                        Quizserver.users.get(username).equals(password)) {
                        success = true;
                        message = "✅ Đăng nhập thành công!";
                    } else {
                        message = "❌ Sai tài khoản hoặc mật khẩu!";
                    }
                }

                oos.writeBoolean(success);
                oos.flush();
                if (!success) {
                    oos.writeObject(message);
                    oos.flush();
                    continue; // quay lại vòng lặp chờ lệnh mới
                }

                // Nếu login/register thành công → gửi danh sách câu hỏi
                oos.writeObject(Quizserver.questions);
                oos.flush();

                // Nhận danh sách đáp án từ client
                @SuppressWarnings("unchecked")
                List<Integer> answers = (List<Integer>) ois.readObject();

                // Chấm điểm
                int score = 0;
                for (int i = 0; i < answers.size(); i++) {
                    if (i < Quizserver.questions.size() &&
                        answers.get(i) == Quizserver.questions.get(i).getCorrect()) {
                        score++;
                    }
                }

                // Gửi điểm cho client
                oos.writeInt(score);
                oos.flush();
            }

        } catch (Exception e) {
            System.out.println("❌ Client disconnected: " + socket);
        } finally {
            try {
                if (ois != null) ois.close();
                if (oos != null) oos.close();
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }
}