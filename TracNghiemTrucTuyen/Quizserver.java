package Quiz;

import java.io.*;
import java.net.*;
import java.util.*;

public class Quizserver {
    private static final int PORT = 5555;
    protected static List<Question> questions = new ArrayList<>();
    protected static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) {
        loadQuestions("src/Question.txt");
        loadUsers("src/users.txt");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🔗 New client connected: " + socket);
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadQuestions(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String questionText = line;
                List<String> options = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    options.add(br.readLine());
                }
                int correct = Integer.parseInt(br.readLine().trim());
                questions.add(new Question(questionText, options, correct));
            }
            System.out.println("📘 Loaded " + questions.size() + " questions.");
        } catch (Exception e) {
            System.err.println("⚠ Lỗi load Question.txt, kiểm tra đường dẫn!");
        }
    }

    protected static void loadUsers(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            System.out.println("👥 Loaded " + users.size() + " users.");
        } catch (Exception e) {
            System.out.println("⚠ Chưa có file users.txt, sẽ tạo mới khi đăng ký.");
        }
    }

    protected static void saveUsers(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}