package Quiz;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class Quizclient extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    private static final int TIME_LIMIT = 60; // 60 giây

    private JTextArea txtQuestion;
    private JRadioButton[] rbOptions;
    private ButtonGroup bg;
    private JButton btnNext, btnSubmit, btnPrevious;
    private JLabel lblTimer;
    private JProgressBar progressBar;
    private List<Question> questions;
    private int currentIndex = 0;
    private Integer[] userAnswers;
    private Timer timer;
    private int timeRemaining;
    private boolean testCompleted = false;
    private String currentUsername = "";

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Quizclient() {
        setTitle("Quiz Client - Online Test");
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 240, 245));

        // Panel timer và progress bar
        JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
        lblTimer = new JLabel("Thời gian còn lại: 01:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 18));
        lblTimer.setForeground(new Color(200, 0, 0));
        pnlTop.add(lblTimer, BorderLayout.NORTH);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 14));
        progressBar.setForeground(new Color(0, 150, 0));
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        pnlTop.add(progressBar, BorderLayout.CENTER);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlTop, BorderLayout.NORTH);

        // Panel câu hỏi
        txtQuestion = new JTextArea();
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new Font("Arial", Font.BOLD, 16));
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setMargin(new Insets(15, 15, 15, 15));
        txtQuestion.setBackground(new Color(255, 255, 255));
        JScrollPane questionScrollPane = new JScrollPane(txtQuestion);
        questionScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1, true),
                "Câu hỏi"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        questionScrollPane.setPreferredSize(new Dimension(400, 400));
        add(questionScrollPane, BorderLayout.WEST);

        // Panel đáp án - GridLayout 2x2
        JPanel pnlOptions = new JPanel(new GridLayout(2, 2, 15, 15));
        pnlOptions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1, true),
                "Chọn đáp án"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        pnlOptions.setBackground(new Color(240, 240, 245));
        rbOptions = new JRadioButton[4];
        bg = new ButtonGroup();
        Border optionBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        for (int i = 0; i < 4; i++) {
            rbOptions[i] = new JRadioButton();
            rbOptions[i].setFont(new Font("Arial", Font.PLAIN, 14));
            rbOptions[i].setBorder(optionBorder);
            rbOptions[i].setBackground(Color.WHITE);
            rbOptions[i].setOpaque(true);
            rbOptions[i].setFocusPainted(false); // Loại bỏ viền focus khi chọn
            bg.add(rbOptions[i]);
            pnlOptions.add(rbOptions[i]);

            final int index = i;
            rbOptions[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!rbOptions[index].isSelected()) {
                        rbOptions[index].setBackground(new Color(230, 230, 250));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!rbOptions[index].isSelected()) {
                        rbOptions[index].setBackground(Color.WHITE);
                    }
                }
            });

            rbOptions[i].addActionListener(e -> {
                for (JRadioButton rb : rbOptions) {
                    rb.setBackground(Color.WHITE);
                }
                rbOptions[index].setBackground(new Color(200, 220, 255));
            });
        }
        JScrollPane optionsScrollPane = new JScrollPane(pnlOptions);
        optionsScrollPane.setPreferredSize(new Dimension(400, 400));
        add(optionsScrollPane, BorderLayout.EAST);

        // Panel buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPrevious = new JButton("<< Câu trước");
        btnNext = new JButton("Câu tiếp theo >>");
        btnSubmit = new JButton("Nộp bài");
        btnPrevious.setFont(new Font("Arial", Font.PLAIN, 14));
        btnNext.setFont(new Font("Arial", Font.PLAIN, 14));
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(0, 102, 204)); // Xanh nước biển
        btnSubmit.setForeground(Color.WHITE);
        btnPrevious.setFocusPainted(false); // Loại bỏ viền focus
        btnNext.setFocusPainted(false);     // Loại bỏ viền focus
        btnSubmit.setFocusPainted(false);   // Loại bỏ viền focus

        pnlButtons.add(btnPrevious);
        pnlButtons.add(btnNext);
        pnlButtons.add(btnSubmit);
        add(pnlButtons, BorderLayout.SOUTH);

        btnPrevious.addActionListener(e -> previousQuestion());
        btnNext.addActionListener(e -> nextQuestion());
        btnSubmit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn nộp bài không?\nThời gian đã sử dụng: " + (TIME_LIMIT - timeRemaining) + " giây.",
                "Xác nhận nộp bài",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                submitAnswers();
            }
        });

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Authentication loop
            boolean authenticated = false;
            while (!authenticated) {
                String[] options = {"Đăng nhập", "Đăng ký"};
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Chào mừng đến với hệ thống trắc nghiệm online!\n\n" +
                    "Đăng ký: Tạo tài khoản mới\n" +
                    "Đăng nhập: Sử dụng tài khoản đã có\n\n" +
                    "Bạn muốn làm gì?",
                    "Hệ thống trắc nghiệm online",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );

                if (choice == -1) {
                    System.exit(0);
                }

                LoginInfo loginInfo = showLoginDialog(choice == 1);
                
                if (loginInfo == null) {
                    continue;
                }

                if (choice == 1) {
                    oos.writeObject("register");
                } else {
                    oos.writeObject("login");
                }
                oos.writeObject(loginInfo.username);
                oos.writeObject(loginInfo.password);
                oos.flush();

                boolean success = ois.readBoolean();
                if (!success) {
                    String errorMessage = (String) ois.readObject();
                    JOptionPane.showMessageDialog(this, "Lỗi: " + errorMessage, "Thất bại", JOptionPane.ERROR_MESSAGE);
                } else {
                    authenticated = true;
                    currentUsername = loginInfo.username;
                    if (choice == 1) {
                        JOptionPane.showMessageDialog(this, 
                            "Đăng ký thành công!\n\n" +
                            "Tài khoản: " + loginInfo.username + "\n" +
                            "Bạn có thể đăng nhập ngay bây giờ.", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Đăng nhập thành công!\n\n" +
                            "Chào mừng: " + loginInfo.username + "\n" +
                            "Chuẩn bị làm bài...", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            questions = (List<Question>) ois.readObject();
            userAnswers = new Integer[questions.size()];
            for (int i = 0; i < userAnswers.length; i++) userAnswers[i] = -1;

            System.out.println("Client nhận được " + questions.size() + " câu hỏi từ server.");
            
            updateProgressBar();
            startQuiz();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!\n\nVui lòng kiểm tra:\n- Server có đang chạy?\n- Cổng 5555 có bị chặn?", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static class LoginInfo {
        String username;
        String password;
        
        LoginInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private LoginInfo showLoginDialog(boolean isRegister) {
        JDialog dialog = new JDialog(this, isRegister ? "Đăng ký tài khoản" : "Đăng nhập", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(240, 240, 245)); // Nền hiện đại

        // Panel chứa form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Tiêu đề
        JLabel titleLabel = new JLabel(isRegister ? "ĐĂNG KÝ TÀI KHOẢN MỚI" : "ĐĂNG NHẬP HỆ THỐNG");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204)); // Xanh nước biển
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        // Hướng dẫn
        if (isRegister) {
            JTextArea helpText = new JTextArea("- Tên đăng nhập: 3-20 ký tự\n- Mật khẩu: ít nhất 3 ký tự\n- Không được để trống");
            helpText.setEditable(false);
            helpText.setFont(new Font("Arial", Font.PLAIN, 12));
            helpText.setBackground(new Color(240, 240, 245));
            helpText.setForeground(Color.GRAY);
            gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
            formPanel.add(helpText, gbc);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 240, 245));
        JButton okButton = new JButton(isRegister ? "Đăng ký" : "Đăng nhập");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(new Color(0, 102, 204)); // Xanh nước biển
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createLineBorder(new Color(0, 80, 160), 1, true));
        okButton.setFocusPainted(false); // Loại bỏ viền focus

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(200, 0, 0));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createLineBorder(new Color(160, 0, 0), 1, true));
        cancelButton.setFocusPainted(false); // Loại bỏ viền focus

        // Hiệu ứng hover cho nút
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(new Color(0, 120, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(new Color(0, 102, 204));
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(220, 0, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(200, 0, 0));
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        final LoginInfo[] result = {null};

        ActionListener submitAction = e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên đăng nhập không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isRegister) {
                if (username.length() < 3 || username.length() > 20) {
                    JOptionPane.showMessageDialog(dialog, "Tên đăng nhập phải từ 3-20 ký tự!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (password.length() < 3) {
                    JOptionPane.showMessageDialog(dialog, "Mật khẩu phải có ít nhất 3 ký tự!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            result[0] = new LoginInfo(username, password);
            dialog.dispose();
        };

        okButton.addActionListener(submitAction);
        cancelButton.addActionListener(e -> dialog.dispose());

        usernameField.addActionListener(submitAction);
        passwordField.addActionListener(submitAction);

        // Hiệu ứng focus cho trường nhập liệu
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 102, 204), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 102, 204), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        usernameField.requestFocus();
        dialog.setVisible(true);

        return result[0];
    }

    private void startQuiz() {
        JOptionPane.showMessageDialog(this, 
            "CHUẨN BỊ BÀI THI\n\n" +
            "Số câu hỏi: " + questions.size() + " câu\n" +
            "Thời gian làm bài: " + TIME_LIMIT + " giây\n" +
            "Có thể quay lại câu trước\n" +
            "Nhấn 'Nộp bài' khi hoàn thành\n\n" +
            "Chúc bạn làm bài tốt!", 
            "Bắt đầu làm bài", JOptionPane.INFORMATION_MESSAGE);
        
        if (questions != null && !questions.isEmpty()) {
            loadQuestion(0);
            startTimer();
        } else {
            JOptionPane.showMessageDialog(this, "Không nhận được câu hỏi từ server!");
        }
    }

    private void startTimer() {
        timeRemaining = TIME_LIMIT;
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
                
                if (timeRemaining <= 10) {
                    lblTimer.setForeground(Color.RED);
                    if (timeRemaining <= 5) {
                        lblTimer.setBackground(Color.YELLOW);
                        lblTimer.setOpaque(true);
                    }
                }
                
                if (timeRemaining <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(Quizclient.this, 
                        "HẾT THỜI GIAN!\n\nBài thi sẽ được nộp tự động.", 
                        "Hết giờ", JOptionPane.WARNING_MESSAGE);
                    submitAnswers();
                }
            }
        });
        timer.start();
    }

    private void updateProgressBar() {
        int answered = 0;
        for (Integer ans : userAnswers) {
            if (ans != null && ans != -1) answered++;
        }
        int progress = (int) ((answered / (double) questions.size()) * 100);
        progressBar.setValue(progress);
        progressBar.setString("Đã trả lời: " + answered + "/" + questions.size());
    }

    private void loadQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            currentIndex = index;
            Question q = questions.get(index);
            txtQuestion.setText("Câu " + (index + 1) + "/" + questions.size() + ":\n\n" + q.getQuestionText());
            txtQuestion.setCaretPosition(0);
            List<String> opts = q.getOptions();
            bg.clearSelection();
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText((char)('A' + i) + ". " + opts.get(i));
                rbOptions[i].setSelected(userAnswers[index] == i);
                rbOptions[i].setBackground(userAnswers[index] == i ? new Color(200, 220, 255) : Color.WHITE);
            }
            
            btnPrevious.setEnabled(currentIndex > 0);
            btnNext.setEnabled(currentIndex < questions.size() - 1);
            updateProgressBar();
        }
    }

    private void previousQuestion() {
        saveCurrentAnswer();
        if (currentIndex > 0) {
            currentIndex--;
            loadQuestion(currentIndex);
        }
    }

    private void nextQuestion() {
        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            loadQuestion(currentIndex);
        }
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < 4; i++) {
            if (rbOptions[i].isSelected()) {
                userAnswers[currentIndex] = i;
                break;
            }
        }
        updateProgressBar();
    }

    private void submitAnswers() {
        if (testCompleted) return;
        
        testCompleted = true;
        if (timer != null) {
            timer.stop();
        }
        
        try {
            saveCurrentAnswer();

            List<Integer> answersList = new ArrayList<>();
            for (Integer ans : userAnswers) {
                answersList.add(ans != null ? ans : -1);
            }

            oos.writeObject(answersList);
            oos.flush();

            int score = ois.readInt();
            int timeTaken = TIME_LIMIT - timeRemaining; // Thời gian hoàn thành (giây)
            
            saveResultToFile(currentUsername, score, timeTaken);
            showResults(score, timeTaken);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveResultToFile(String username, int score, int timeTaken) {
        try (FileWriter fw = new FileWriter("src/client_results.txt", true)) {
            double percentage = (double) score / questions.size() * 100;
            fw.write(username + "," + score + "," + questions.size() + "," + String.format("%.1f", percentage) + "," + timeTaken + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showResults(int score, int timeTaken) {
        double percentage = (double) score / questions.size() * 100;
        String grade;
        Color gradeColor;
        String symbol;
        
        if (percentage >= 80) {
            grade = "Xuất sắc";
            gradeColor = new Color(0, 150, 0);
            symbol = "[A+]";
        } else if (percentage >= 70) {
            grade = "Giỏi";
            gradeColor = new Color(0, 102, 204);
            symbol = "[A]";
        } else if (percentage >= 60) {
            grade = "Khá";
            gradeColor = Color.ORANGE;
            symbol = "[B]";
        } else if (percentage >= 50) {
            grade = "Trung bình";
            gradeColor = Color.MAGENTA;
            symbol = "[C]";
        } else {
            grade = "Yếu";
            gradeColor = Color.RED;
            symbol = "[F]";
        }
        
        JDialog resultDialog = new JDialog(this, "KẾT QUẢ BÀI THI", true);
        resultDialog.setLayout(new BorderLayout());
        resultDialog.setSize(450, 400);
        resultDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(240, 240, 245));
        
        JLabel titleLabel = new JLabel("HOÀN THÀNH BÀI THI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        JLabel scoreLabel = new JLabel("Điểm số: " + score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel percentLabel = new JLabel("Tỷ lệ đúng: " + String.format("%.1f", percentage) + "%", SwingConstants.CENTER);
        percentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel gradeLabel = new JLabel(symbol + " Xếp loại: " + grade, SwingConstants.CENTER);
        gradeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gradeLabel.setForeground(gradeColor);
        
        JLabel timeLabel = new JLabel("Thời gian hoàn thành: " + timeTaken + " giây", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeLabel.setForeground(Color.BLUE);
        
        JLabel congratsLabel = new JLabel("Cảm ơn bạn đã tham gia!", SwingConstants.CENTER);
        congratsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        congratsLabel.setForeground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 245));
        
        JButton rankingButton = new JButton("Xem bảng xếp hạng");
        rankingButton.setFont(new Font("Arial", Font.BOLD, 12));
        rankingButton.setBackground(new Color(0, 102, 204));
        rankingButton.setForeground(Color.WHITE);
        rankingButton.setFocusPainted(false); // Loại bỏ viền focus
        rankingButton.addActionListener(e -> {
            resultDialog.dispose();
            showRanking();
        });
        
        JButton historyButton = new JButton("Xem lịch sử");
        historyButton.setFont(new Font("Arial", Font.BOLD, 12));
        historyButton.setBackground(new Color(0, 102, 204));
        historyButton.setForeground(Color.WHITE);
        historyButton.setFocusPainted(false); // Loại bỏ viền focus
        historyButton.addActionListener(e -> showHistory());
        
        JButton closeButton = new JButton("Đóng chương trình");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(new Color(200, 0, 0));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false); // Loại bỏ viền focus
        closeButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(rankingButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(closeButton);
        
        contentPanel.add(titleLabel);
        contentPanel.add(scoreLabel);
        contentPanel.add(percentLabel);
        contentPanel.add(gradeLabel);
        contentPanel.add(timeLabel);
        contentPanel.add(congratsLabel);
        contentPanel.add(new JLabel());
        
        resultDialog.add(contentPanel, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        resultDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        resultDialog.setVisible(true);
    }

    private static class RankingEntry {
        String username;
        int score;
        int totalQuestions;
        double percentage;
        int timeTaken; // Thêm thời gian hoàn thành
        
        RankingEntry(String username, int score, int totalQuestions, double percentage, int timeTaken) {
            this.username = username;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.percentage = percentage;
            this.timeTaken = timeTaken;
        }
    }

    private void showRanking() {
        List<RankingEntry> rankings = loadRankings();
        
        JDialog rankingDialog = new JDialog(this, "BẢNG XẾP HẠNG TOP PLAYERS", true);
        rankingDialog.setSize(600, 400);
        rankingDialog.setLocationRelativeTo(this);
        rankingDialog.setLayout(new BorderLayout());
        rankingDialog.getContentPane().setBackground(new Color(240, 240, 245));
        
        JLabel titleLabel = new JLabel("TOP 10 ĐIỂM CAO NHẤT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        String[] columnNames = {"Hạng", "Tên người chơi", "Điểm số", "Tỷ lệ %", "Thời gian (giây)", "Xếp loại"};
        String[][] data = new String[Math.min(rankings.size(), 10)][6];
        
        for (int i = 0; i < Math.min(rankings.size(), 10); i++) {
            RankingEntry entry = rankings.get(i);
            String rank = "";
            
            if (i == 0) rank = "[1st]";
            else if (i == 1) rank = "[2nd]";
            else if (i == 2) rank = "[3rd]";
            else rank = String.valueOf(i + 1);
            
            String grade = "";
            if (entry.percentage >= 80) grade = "Xuất sắc [A+]";
            else if (entry.percentage >= 70) grade = "Giỏi [A]";
            else if (entry.percentage >= 60) grade = "Khá [B]";
            else if (entry.percentage >= 50) grade = "Trung bình [C]";
            else grade = "Yếu [F]";
            
            data[i][0] = rank;
            data[i][1] = entry.username;
            data[i][2] = entry.score + "/" + entry.totalQuestions;
            data[i][3] = String.format("%.1f%%", entry.percentage);
            data[i][4] = String.valueOf(entry.timeTaken); // Thêm cột thời gian
            data[i][5] = grade;
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(200, 200, 200));
        table.setRowHeight(25);
        table.setGridColor(new Color(220, 220, 220));
        table.setFocusTraversalKeysEnabled(false); // Ngăn focus tự động
        table.setFocusable(false); // Loại bỏ focus cho bảng
        table.setRowSelectionAllowed(false); // Loại bỏ chọn hàng

        // Xóa hiệu ứng hover
        table.addMouseMotionListener(null);
        table.addMouseListener(null);

        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(240, 240, 245));
        JLabel infoLabel = new JLabel("Bảng xếp hạng được cập nhật theo thời gian thực");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 245));
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(new Color(200, 0, 0));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false); // Loại bỏ viền focus
        closeButton.addActionListener(e -> rankingDialog.dispose());
        buttonPanel.add(closeButton);
        
        rankingDialog.add(titleLabel, BorderLayout.NORTH);
        rankingDialog.add(scrollPane, BorderLayout.CENTER);
        rankingDialog.add(infoPanel, BorderLayout.SOUTH);
        rankingDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        rankingDialog.setVisible(true);
    }

    private void showHistory() {
        List<RankingEntry> history = loadHistory();
        
        JDialog historyDialog = new JDialog(this, "LỊCH SỬ LÀM BÀI CỦA " + currentUsername, true);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        historyDialog.getContentPane().setBackground(new Color(240, 240, 245));
        
        JLabel titleLabel = new JLabel("LỊCH SỬ LÀM BÀI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        String[] columnNames = {"STT", "Điểm số", "Tỷ lệ %", "Thời gian (giây)", "Xếp loại"};
        String[][] data = new String[history.size()][5];
        
        for (int i = 0; i < history.size(); i++) {
            RankingEntry entry = history.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = entry.score + "/" + entry.totalQuestions;
            data[i][2] = String.format("%.1f%%", entry.percentage);
            data[i][3] = String.valueOf(entry.timeTaken);
            String grade = "";
            if (entry.percentage >= 80) grade = "Xuất sắc [A+]";
            else if (entry.percentage >= 70) grade = "Giỏi [A]";
            else if (entry.percentage >= 60) grade = "Khá [B]";
            else if (entry.percentage >= 50) grade = "Trung bình [C]";
            else grade = "Yếu [F]";
            data[i][4] = grade;
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(200, 200, 200));
        table.setRowHeight(25);
        table.setGridColor(new Color(220, 220, 220));
        table.setFocusTraversalKeysEnabled(false); // Ngăn focus tự động
        table.setFocusable(false); // Loại bỏ focus cho bảng
        table.setRowSelectionAllowed(false); // Loại bỏ chọn hàng

        // Xóa hiệu ứng hover
        table.addMouseMotionListener(null);
        table.addMouseListener(null);

        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 245));
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(new Color(200, 0, 0));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false); // Loại bỏ viền focus
        closeButton.addActionListener(e -> historyDialog.dispose());
        buttonPanel.add(closeButton);
        
        historyDialog.add(titleLabel, BorderLayout.NORTH);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.setVisible(true);
    }

    private List<RankingEntry> loadHistory() {
        List<RankingEntry> history = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("src/client_results.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5 && parts[0].equals(currentUsername)) { // Lọc theo username
                    int score = Integer.parseInt(parts[1]);
                    int totalQuestions = Integer.parseInt(parts[2]);
                    double percentage = Double.parseDouble(parts[3]);
                    int timeTaken = Integer.parseInt(parts[4]);
                    
                    history.add(new RankingEntry(currentUsername, score, totalQuestions, percentage, timeTaken));
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi đọc lịch sử hoặc chưa có dữ liệu.");
        }
        
        // Sắp xếp theo thời gian gần nhất (giả sử không có timestamp, sắp xếp theo thứ tự file)
        return history;
    }

    private List<RankingEntry> loadRankings() {
        List<RankingEntry> rankings = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("src/client_results.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) { // Kiểm tra 5 cột
                    String username = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    int totalQuestions = Integer.parseInt(parts[2]);
                    double percentage = Double.parseDouble(parts[3]);
                    int timeTaken = Integer.parseInt(parts[4]);
                    
                    rankings.add(new RankingEntry(username, score, totalQuestions, percentage, timeTaken));
                }
            }
        } catch (Exception e) {
            System.out.println("Chưa có dữ liệu ranking hoặc lỗi đọc file.");
        }
        
        Collections.sort(rankings, new Comparator<RankingEntry>() {
            @Override
            public int compare(RankingEntry a, RankingEntry b) {
                if (a.score != b.score) {
                    return Integer.compare(b.score, a.score); // Sắp xếp theo điểm giảm dần
                }
                return Integer.compare(a.timeTaken, b.timeTaken); // Nếu điểm bằng, sắp xếp theo thời gian tăng dần
            }
        });
        
        return rankings;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Quizclient().setVisible(true));
    }
}