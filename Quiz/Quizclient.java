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
    private JButton btnNext, btnSubmit, btnPrevious, btnExit;
    private JLabel lblTimer, lblQuestionNumber;
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
    private Socket socket;

    public Quizclient() {
        setTitle("Quiz Client - Online Test");
        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 250));

        // Panel timer và progress bar
        JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
        lblTimer = new JLabel("Thời gian còn lại: 01:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTimer.setForeground(new Color(220, 20, 60));
        lblTimer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        pnlTop.add(lblTimer, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressBar.setForeground(new Color(40, 167, 69));
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        pnlTop.add(progressBar, BorderLayout.CENTER);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlTop.setBackground(new Color(245, 245, 250));
        add(pnlTop, BorderLayout.NORTH);

        // Panel câu hỏi
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBackground(new Color(245, 245, 250));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblQuestionNumber = new JLabel("Câu 1/" + (questions != null ? questions.size() : 0));
        lblQuestionNumber.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQuestionNumber.setForeground(new Color(0, 123, 255));
        questionPanel.add(lblQuestionNumber, BorderLayout.NORTH);

        txtQuestion = new JTextArea();
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setMargin(new Insets(15, 15, 15, 15));
        txtQuestion.setBackground(Color.WHITE);
        txtQuestion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane questionScrollPane = new JScrollPane(txtQuestion);
        questionScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(120, 120, 120), 1, true),
            "Nội dung câu hỏi",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        questionPanel.add(questionScrollPane, BorderLayout.CENTER);
        add(questionPanel, BorderLayout.CENTER);

        // Panel chứa đáp án và nút
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(245, 245, 250));

        // Panel đáp án
        JPanel pnlOptions = new JPanel(new GridLayout(4, 1, 5, 10));
        pnlOptions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120), 1, true),
                "Lựa chọn đáp án",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        pnlOptions.setBackground(new Color(245, 245, 250));
        rbOptions = new JRadioButton[4];
        bg = new ButtonGroup();
        Border optionBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        );

        for (int i = 0; i < 4; i++) {
            rbOptions[i] = new JRadioButton();
            rbOptions[i].setFont(new Font("Segoe UI", Font.PLAIN, 15));
            rbOptions[i].setBorder(optionBorder);
            rbOptions[i].setBackground(Color.WHITE);
            rbOptions[i].setOpaque(true);
            rbOptions[i].setFocusPainted(false);
            rbOptions[i].setHorizontalAlignment(SwingConstants.LEFT);
            rbOptions[i].setMargin(new Insets(5, 5, 5, 5));
            bg.add(rbOptions[i]);
            pnlOptions.add(rbOptions[i]);

            final int index = i;
            rbOptions[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!rbOptions[index].isSelected()) {
                        rbOptions[index].setBackground(new Color(230, 240, 255));
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
        bottomPanel.add(pnlOptions, BorderLayout.CENTER);

        // Panel nút
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPrevious = new JButton("<< Câu trước");
        btnNext = new JButton("Câu tiếp theo >>");
        btnSubmit = new JButton("Nộp bài");
        btnExit = new JButton("Thoát");
        
        JButton[] buttons = {btnPrevious, btnNext, btnSubmit, btnExit};
        for (JButton btn : buttons) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setFocusPainted(false);
        }

        btnPrevious.setBackground(new Color(108, 117, 125));
        btnNext.setBackground(new Color(108, 117, 125));
        btnSubmit.setBackground(new Color(0, 123, 255));
        btnExit.setBackground(new Color(220, 53, 69));
        
        btnPrevious.setForeground(Color.WHITE);
        btnNext.setForeground(Color.WHITE);
        btnSubmit.setForeground(Color.WHITE);
        btnExit.setForeground(Color.WHITE);

        btnPrevious.addMouseListener(createHoverListener(new Color(108, 117, 125), new Color(130, 140, 150)));
        btnNext.addMouseListener(createHoverListener(new Color(108, 117, 125), new Color(130, 140, 150)));
        btnSubmit.addMouseListener(createHoverListener(new Color(0, 123, 255), new Color(30, 144, 255)));
        btnExit.addMouseListener(createHoverListener(new Color(220, 53, 69), new Color(240, 73, 89)));

        pnlButtons.add(btnPrevious);
        pnlButtons.add(btnNext);
        pnlButtons.add(btnSubmit);
        pnlButtons.add(btnExit);
        pnlButtons.setBackground(new Color(245, 245, 250));
        bottomPanel.add(pnlButtons, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

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
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                testCompleted ? 
                    "Bạn có muốn thoát chương trình không?" :
                    "Bạn có muốn thoát chương trình không?\nKết quả bài thi sẽ không được lưu nếu chưa nộp bài!",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                cleanupAndExit();
            }
        });

        connectToServer();
    }

    private MouseAdapter createHoverListener(Color normalColor, Color hoverColor) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton)e.getSource()).setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton)e.getSource()).setBackground(normalColor);
            }
        };
    }

    private void connectToServer() {
        try {
            int choice = -1;
            boolean authenticated = false;
            String lastUsername = "";
            String lastPassword = "";

            while (choice == -1 || !authenticated) {
                if (choice == -1) {
                    String[] options = {"Đăng nhập", "Đăng ký"};
                    choice = JOptionPane.showOptionDialog(
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
                        cleanupAndExit();
                        return;
                    }
                } else {
                    // Đóng kết nối cũ nếu tồn tại
                    if (socket != null && !socket.isClosed()) {
                        try { socket.close(); } catch (IOException e) {}
                        oos = null;
                        ois = null;
                    }

                    // Tạo kết nối mới
                    socket = new Socket(SERVER_HOST, SERVER_PORT);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    LoginInfo loginInfo = showLoginDialog(choice == 1, lastUsername, lastPassword);

                    if (loginInfo == null) {
                        choice = -1; // Quay lại chọn Đăng nhập/Đăng ký
                        continue;
                    }

                    lastUsername = loginInfo.username;
                    lastPassword = loginInfo.password;

                    if (choice == 1) {
                        oos.writeObject("register");
                    } else {
                        oos.writeObject("login");
                    }
                    oos.writeObject(loginInfo.username);
                    oos.writeObject(loginInfo.password);
                    oos.flush();

                    if (socket.isClosed()) {
                        throw new IOException("Kết nối đã bị đóng.");
                    }

                    boolean success = ois.readBoolean();
                    if (!success) {
                        String errorMessage = (String) ois.readObject();
                        JOptionPane.showMessageDialog(this, "Lỗi: " + errorMessage, "Thất bại", JOptionPane.ERROR_MESSAGE);
                        continue;
                    } else {
                        authenticated = true;
                        currentUsername = loginInfo.username;
                        if (choice == 1) {
                            JOptionPane.showMessageDialog(this, 
                                "Đăng ký thành công!\n\nTài khoản: " + loginInfo.username + "\nBạn có thể đăng nhập ngay bây giờ.", 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Đăng nhập thành công!\n\nChào mừng: " + loginInfo.username + "\nChuẩn bị làm bài...", 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        }
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
            cleanupAndExit();
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

    private LoginInfo showLoginDialog(boolean isRegister, String lastUsername, String lastPassword) {
        JDialog dialog = new JDialog(this, isRegister ? "Đăng ký tài khoản" : "Đăng nhập", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(245, 245, 250));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel(isRegister ? "ĐĂNG KÝ TÀI KHOẢN MỚI" : "ĐĂNG NHẬP HỆ THỐNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        if (!lastUsername.isEmpty()) {
            usernameField.setText(lastUsername);
        }
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        if (!lastPassword.isEmpty()) {
            passwordField.setText(lastPassword);
        }
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        if (isRegister) {
            JTextArea helpText = new JTextArea("- Tên đăng nhập: 3-20 ký tự\n- Mật khẩu: ít nhất 3 ký tự\n- Không được để trống");
            helpText.setEditable(false);
            helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            helpText.setBackground(new Color(245, 245, 250));
            helpText.setForeground(Color.GRAY);
            gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
            formPanel.add(helpText, gbc);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));
        JButton okButton = new JButton(isRegister ? "Đăng ký" : "Đăng nhập");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setBackground(new Color(0, 123, 255));
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 1, true));
        okButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createLineBorder(new Color(200, 30, 50), 1, true));
        cancelButton.setFocusPainted(false);

        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(new Color(30, 144, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(new Color(0, 123, 255));
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(240, 73, 89));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(220, 53, 69));
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        final LoginInfo[] result = {null};

        ActionListener submitAction = e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên đăng nhập và mật khẩu không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
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

        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        usernameField.requestFocus();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
            lblQuestionNumber.setText("Câu " + (index + 1) + "/" + questions.size());
            txtQuestion.setText(q.getQuestionText());
            txtQuestion.setCaretPosition(0);
            List<String> opts = q.getOptions();
            bg.clearSelection();
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText((char)('A' + i) + ". " + opts.get(i));
                rbOptions[i].setSelected(userAnswers[index] != null && userAnswers[index] == i);
                rbOptions[i].setBackground(userAnswers[index] != null && userAnswers[index] == i ? new Color(200, 220, 255) : Color.WHITE);
            }
            
            btnPrevious.setEnabled(currentIndex > 0);
            btnNext.setEnabled(currentIndex < questions.size() - 1);
            updateProgressBar();
            revalidate();
            repaint();
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
            int timeTaken = TIME_LIMIT - timeRemaining;
            
            try (FileWriter fw = new FileWriter("src/client_results.txt", true)) {
                double percentage = (double) score / questions.size() * 100;
                fw.write(currentUsername + "," + score + "," + questions.size() + "," + String.format("%.1f", percentage) + "," + timeTaken + "\n");
            }
            showResults(score, timeTaken);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            cleanupAndExit();
        }
    }

    private void showResults(int score, int timeTaken) {
        double percentage = (double) score / questions.size() * 100;
        String grade;
        Color gradeColor;
        String symbol;
        
        if (percentage >= 80) { grade = "Xuất sắc"; gradeColor = new Color(40, 167, 69); symbol = "[A+]"; }
        else if (percentage >= 70) { grade = "Giỏi"; gradeColor = new Color(0, 123, 255); symbol = "[A]"; }
        else if (percentage >= 60) { grade = "Khá"; gradeColor = new Color(255, 193, 7); symbol = "[B]"; }
        else if (percentage >= 50) { grade = "Trung bình"; gradeColor = new Color(111, 66, 193); symbol = "[C]"; }
        else { grade = "Yếu"; gradeColor = new Color(220, 53, 69); symbol = "[F]"; }
        
        JDialog resultDialog = new JDialog(this, "KẾT QUẢ BÀI THI", true);
        resultDialog.setLayout(new BorderLayout());
        resultDialog.setSize(450, 400);
        resultDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(245, 245, 250));
        
        JLabel titleLabel = new JLabel("HOÀN THÀNH BÀI THI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 123, 255));
        
        JLabel scoreLabel = new JLabel("Điểm số: " + score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel percentLabel = new JLabel("Tỷ lệ đúng: " + String.format("%.1f", percentage) + "%", SwingConstants.CENTER);
        percentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel gradeLabel = new JLabel(symbol + " Xếp loại: " + grade, SwingConstants.CENTER);
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gradeLabel.setForeground(gradeColor);
        
        JLabel timeLabel = new JLabel("Thời gian hoàn thành: " + timeTaken + " giây", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(0, 123, 255));
        
        JLabel congratsLabel = new JLabel("Cảm ơn bạn đã tham gia!", SwingConstants.CENTER);
        congratsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        congratsLabel.setForeground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));
        
        JButton rankingButton = new JButton("Xem bảng xếp hạng");
        rankingButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rankingButton.setBackground(new Color(0, 123, 255));
        rankingButton.setForeground(Color.WHITE);
        rankingButton.setFocusPainted(false);
        rankingButton.addActionListener(e -> {
            resultDialog.setVisible(false);
            showRanking(resultDialog);
        });
        
        JButton viewAnswersButton = new JButton("Xem đáp án");
        viewAnswersButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAnswersButton.setBackground(new Color(0, 123, 255));
        viewAnswersButton.setForeground(Color.WHITE);
        viewAnswersButton.setFocusPainted(false);
        viewAnswersButton.addActionListener(e -> {
            resultDialog.setVisible(false);
            verifyAnswers();
            resultDialog.setVisible(true);
        });
        
        JButton closeButton = new JButton("Thoát");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            resultDialog.dispose();
            cleanupAndExit();
        });
        
        buttonPanel.add(rankingButton);
        buttonPanel.add(viewAnswersButton);
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
        resultDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        resultDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resultDialog.dispose();
                cleanupAndExit();
            }
        });
        resultDialog.setVisible(true);
    }

    private static class RankingEntry {
        String username;
        int score;
        int totalQuestions;
        double percentage;
        int timeTaken;
        
        RankingEntry(String username, int score, int totalQuestions, double percentage, int timeTaken) {
            this.username = username;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.percentage = percentage;
            this.timeTaken = timeTaken;
        }
    }

    private void showRanking(JDialog resultDialog) {
        List<RankingEntry> rankings = loadRankings();
        
        JDialog rankingDialog = new JDialog(this, "BẢNG XẾP HẠNG TOP PLAYERS", true);
        rankingDialog.setSize(600, 400);
        rankingDialog.setLocationRelativeTo(this);
        rankingDialog.setLayout(new BorderLayout());
        rankingDialog.getContentPane().setBackground(new Color(245, 245, 250));
        
        JLabel titleLabel = new JLabel("TOP 10 ĐIỂM CAO NHẤT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        String[] columnNames = {"Hạng", "Tên người chơi", "Điểm số", "Tỷ lệ %", "Thời gian (giây)", "Xếp loại"};
        String[][] data = new String[Math.min(rankings.size(), 10)][6];
        
        for (int i = 0; i < Math.min(rankings.size(), 10); i++) {
            RankingEntry entry = rankings.get(i);
            String rank = (i == 0 ? "[1st]" : i == 1 ? "[2nd]" : i == 2 ? "[3rd]" : String.valueOf(i + 1));
            String grade = entry.percentage >= 80 ? "Xuất sắc [A+]" : entry.percentage >= 70 ? "Giỏi [A]" : entry.percentage >= 60 ? "Khá [B]" : entry.percentage >= 50 ? "Trung bình [C]" : "Yếu [F]";
            
            data[i][0] = rank;
            data[i][1] = entry.username;
            data[i][2] = entry.score + "/" + entry.totalQuestions;
            data[i][3] = String.format("%.1f%%", entry.percentage);
            data[i][4] = String.valueOf(entry.timeTaken);
            data[i][5] = grade;
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(200, 200, 200));
        table.setRowHeight(25);
        table.setGridColor(new Color(220, 220, 220));
        table.setFocusTraversalKeysEnabled(false);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            rankingDialog.dispose();
            resultDialog.setVisible(true);
        });
        
        buttonPanel.add(closeButton);
        
        rankingDialog.add(titleLabel, BorderLayout.NORTH);
        rankingDialog.add(scrollPane, BorderLayout.CENTER);
        rankingDialog.add(buttonPanel, BorderLayout.SOUTH);
        rankingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        rankingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                rankingDialog.dispose();
                resultDialog.setVisible(true);
            }
        });
        rankingDialog.setVisible(true);
    }

    private void verifyAnswers() {
        if (!testCompleted) {
            JOptionPane.showMessageDialog(this, "Vui lòng nộp bài trước khi xem đáp án!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder verificationText = new StringBuilder("<html><body style='font-family: Segoe UI; font-size: 14px;'>");
        int correctCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int userAnswer = userAnswers[i] != null ? userAnswers[i] : -1;
            int correctAnswer = q.getCorrectAnswer();
            verificationText.append("<p>Câu ").append(i + 1).append(": ").append(q.getQuestionText()).append("<br>");

            for (int j = 0; j < 4; j++) {
                String option = (char)('A' + j) + ". " + q.getOptions().get(j);
                if (j == correctAnswer) {
                    verificationText.append("<span style='color: green;'>").append(option).append("</span><br>");
                    if (j == userAnswer) correctCount++;
                } else if (j == userAnswer) {
                    verificationText.append("<span style='color: red;'>").append(option).append("</span><br>");
                } else {
                    verificationText.append(option).append("<br>");
                }
            }
            verificationText.append("</p><hr>");
        }

        verificationText.append("<p>Tổng số câu đúng: ").append(correctCount).append("/").append(questions.size()).append("</p></body></html>");
        JOptionPane.showMessageDialog(this, verificationText.toString(), "Xem đáp án", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<RankingEntry> loadRankings() {
        List<RankingEntry> rankings = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("src/client_results.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    rankings.add(new RankingEntry(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), Integer.parseInt(parts[4])));
                }
            }
        } catch (Exception e) {
            System.out.println("Chưa có dữ liệu ranking hoặc lỗi đọc file.");
        }
        
        Collections.sort(rankings, (a, b) -> {
            if (a.score != b.score) return Integer.compare(b.score, a.score);
            return Integer.compare(a.timeTaken, b.timeTaken);
        });
        
        return rankings;
    }

    private void cleanupAndExit() {
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Client socket closed at " + new java.util.Date());
            }
            System.out.println("Closing client...");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Quizclient().setVisible(true));
    }
}