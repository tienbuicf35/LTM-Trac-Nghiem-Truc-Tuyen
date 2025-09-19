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

    public Quizclient() {
        setTitle("Quiz Client - Online Test");
        setSize(900, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Handle close operation manually
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 250));

        // Panel timer và progress bar
        JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
        lblTimer = new JLabel("Thời gian còn lại: 01:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTimer.setForeground(new Color(220, 20, 60));
        lblTimer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        pnlTop.add(lblTimer, BorderLayout.NORTH);

        // Progress bar
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

        // Question number label
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
        
        // Button styling
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

        // Hover effects
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

        // Button actions
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
        btnExit.addActionListener(e -> confirmExit());

        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
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

    private void confirmExit() {
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
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }
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
                    confirmExit();
                    return;
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
            confirmExit();
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
        dialog.getContentPane().setBackground(new Color(245, 245, 250));

        // Panel chứa form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Tiêu đề
        JLabel titleLabel = new JLabel(isRegister ? "ĐĂNG KÝ TÀI KHOẢN MỚI" : "ĐĂNG NHẬP HỆ THỐNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // Username
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
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        // Password
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
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        // Hướng dẫn
        if (isRegister) {
            JTextArea helpText = new JTextArea("- Tên đăng nhập: 3-20 ký tự\n- Mật khẩu: ít nhất 3 ký tự\n- Không được để trống");
            helpText.setEditable(false);
            helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            helpText.setBackground(new Color(245, 245, 250));
            helpText.setForeground(Color.GRAY);
            gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
            formPanel.add(helpText, gbc);
        }

        // Buttons
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

        // Hiệu ứng hover cho nút
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
            confirmExit();
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
                rbOptions[i].setSelected(userAnswers[index] == i);
                rbOptions[i].setBackground(userAnswers[index] == i ? new Color(200, 220, 255) : Color.WHITE);
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
            gradeColor = new Color(40, 167, 69);
            symbol = "[A+]";
        } else if (percentage >= 70) {
            grade = "Giỏi";
            gradeColor = new Color(0, 123, 255);
            symbol = "[A]";
        } else if (percentage >= 60) {
            grade = "Khá";
            gradeColor = new Color(255, 193, 7);
            symbol = "[B]";
        } else if (percentage >= 50) {
            grade = "Trung bình";
            gradeColor = new Color(111, 66, 193);
            symbol = "[C]";
        } else {
            grade = "Yếu";
            gradeColor = new Color(220, 53, 69);
            symbol = "[F]";
        }
        
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
            resultDialog.dispose();
            showRanking();
        });
        
        JButton historyButton = new JButton("Xem lịch sử");
        historyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyButton.setBackground(new Color(0, 123, 255));
        historyButton.setForeground(Color.WHITE);
        historyButton.setFocusPainted(false);
        historyButton.addActionListener(e -> showHistory());
        
        JButton closeButton = new JButton("Thoát");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> confirmExit());
        
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
        int timeTaken;
        
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
        rankingDialog.getContentPane().setBackground(new Color(245, 245, 250));
        
        JLabel titleLabel = new JLabel("TOP 10 ĐIỂM CAO NHẤT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 123, 255));
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
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(245, 245, 250));
        JLabel infoLabel = new JLabel("Bảng xếp hạng được cập nhật theo thời gian thực");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
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
        historyDialog.getContentPane().setBackground(new Color(245, 245, 250));
        
        JLabel titleLabel = new JLabel("LỊCH SỬ LÀM BÀI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 123, 255));
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
                if (parts.length == 5 && parts[0].equals(currentUsername)) {
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
        
        return history;
    }

    private List<RankingEntry> loadRankings() {
        List<RankingEntry> rankings = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("src/client_results.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
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
                    return Integer.compare(b.score, a.score);
                }
                return Integer.compare(a.timeTaken, b.timeTaken);
            }
        });
        
        return rankings;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Quizclient().setVisible(true));
    }
}