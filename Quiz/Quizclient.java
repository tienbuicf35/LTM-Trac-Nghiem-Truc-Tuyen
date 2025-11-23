package Quiz;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.net.URL; 
import java.util.Random; 

public class Quizclient extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    private static final int TIME_LIMIT_PER_QUESTION = 30; // 30 giây cho MỖI CÂU HỎI
    private static final int SUBMIT_BUTTON_INDEX = 2; // Vị trí cố định của nút Nộp bài trong pnlButtons

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
    private long startTime; 
    private int totalTimeTaken = 0; 
    
    // 🔥 SỬA LỖI QUAN TRỌNG: THÊM BIẾN LƯU KẾT QUẢ CUỐI CÙNG VÀO MEMBER CLASS
    private int finalScore = -1; 

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;

    // --- CÁC MÀU SẮC CHỦ ĐẠO (DARK / DEEP BLUE THEME) ---
    private final Color PRIMARY_COLOR = new Color(64, 144, 245); 
    private final Color BACKGROUND_COLOR = new Color(30, 30, 45); 
    private final Color PANEL_COLOR = new Color(40, 40, 60); 
    private final Color TEXT_COLOR = Color.WHITE; 
    private final Color ACCENT_COLOR = new Color(138, 43, 226); 
    private final Color DANGER_COLOR = new Color(255, 99, 71); 
    private final Color BORDER_COLOR = new Color(90, 90, 110); 

    private class RankCellRenderer extends DefaultTableCellRenderer {
        private final Color GOLD = new Color(255, 215, 0);       
        private final Color SILVER = new Color(192, 192, 192);   
        private final Color BRONZE = new Color(205, 127, 50);    
        private final Color GOLD_BG = new Color(70, 50, 0);      
        private final Color SILVER_BG = new Color(60, 60, 60);   
        private final Color BRONZE_BG = new Color(70, 40, 20);   

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            c.setBackground(row % 2 == 0 ? PANEL_COLOR : new Color(50, 50, 75));
            
            c.setFont(new Font("Dialog", Font.BOLD, 18)); 
            c.setForeground(TEXT_COLOR);
            setHorizontalAlignment(JLabel.CENTER);

            String rankText = String.valueOf(value); 
            setText(rankText);
            
            if (rankText.contains("👑")) { 
                c.setForeground(GOLD.brighter());
                c.setFont(c.getFont().deriveFont(Font.BOLD, 22f)); 
                c.setBackground(GOLD_BG.darker()); 
            } else if (rankText.contains("🥈")) { 
                c.setForeground(SILVER.brighter());
                c.setFont(c.getFont().deriveFont(Font.BOLD, 19f));
                c.setBackground(SILVER_BG.darker());
            } else if (rankText.contains("🥉")) { 
                c.setForeground(BRONZE.brighter());
                c.setFont(c.getFont().deriveFont(Font.BOLD, 19f));
                c.setBackground(BRONZE_BG.darker());
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN, 16f)); 
            }

            if (isSelected) {
                 c.setBackground(PRIMARY_COLOR.darker());
            }

            return c;
        }
    }


    public Quizclient() {
        setTitle("Quiz Client - Online Test");
        setSize(900, 650); 
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BACKGROUND_COLOR);

        try {
            URL iconURL = getClass().getResource("/images/quiz_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                this.setIconImage(icon.getImage());
            } else {
                System.err.println("Icon file not found in Classpath: /images/quiz_icon.png. Check file location!");
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

        // Panel timer và progress bar
        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setBackground(BACKGROUND_COLOR);
        
        lblTimer = new JLabel("Thời gian còn lại: 00:30", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
        lblTimer.setForeground(DANGER_COLOR);
        lblTimer.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        lblTimer.setOpaque(true);
        lblTimer.setBackground(PANEL_COLOR); 
        pnlTop.add(lblTimer, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        progressBar.setBackground(PANEL_COLOR);
        progressBar.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        progressBar.setPreferredSize(new Dimension(100, 25));
        pnlTop.add(progressBar, BorderLayout.CENTER);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        add(pnlTop, BorderLayout.NORTH);

        // Panel câu hỏi
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBackground(BACKGROUND_COLOR);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        lblQuestionNumber = new JLabel("Câu 1/...");
        lblQuestionNumber.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblQuestionNumber.setForeground(PRIMARY_COLOR);
        questionPanel.add(lblQuestionNumber, BorderLayout.NORTH);

        txtQuestion = new JTextArea();
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 17)); 
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setMargin(new Insets(15, 15, 15, 15));
        txtQuestion.setBackground(PANEL_COLOR);
        txtQuestion.setForeground(TEXT_COLOR);
        
        Border questionBorder = new LineBorder(BORDER_COLOR, 1, true);
        txtQuestion.setBorder(BorderFactory.createCompoundBorder(
            questionBorder,
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane questionScrollPane = new JScrollPane(txtQuestion);
        questionScrollPane.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            "NỘI DUNG CÂU HỎI",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14),
            TEXT_COLOR
        ));
        questionScrollPane.setBackground(BACKGROUND_COLOR);
        questionPanel.add(questionScrollPane, BorderLayout.CENTER);
        add(questionPanel, BorderLayout.CENTER);

        // Panel chứa đáp án và nút
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 15));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        // Panel đáp án
        JPanel pnlOptions = new JPanel(new GridLayout(2, 2, 10, 15)); 
        pnlOptions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                "LỰA CHỌN ĐÁP ÁN",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                TEXT_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        pnlOptions.setBackground(BACKGROUND_COLOR);
        rbOptions = new JRadioButton[4];
        bg = new ButtonGroup();
        
        Border optionBorder = BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15) 
        );

        for (int i = 0; i < 4; i++) {
            rbOptions[i] = new JRadioButton();
            rbOptions[i].setFont(new Font("Segoe UI", Font.PLAIN, 16)); 
            rbOptions[i].setBorder(optionBorder);
            rbOptions[i].setBackground(PANEL_COLOR);
            rbOptions[i].setForeground(TEXT_COLOR);
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
                        rbOptions[index].setBackground(new Color(60, 60, 80)); 
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!rbOptions[index].isSelected()) {
                        rbOptions[index].setBackground(PANEL_COLOR); 
                    }
                }
            });

            rbOptions[i].addActionListener(e -> {
                Color selectedColor = PRIMARY_COLOR.darker();
                for (JRadioButton rb : rbOptions) {
                    rb.setBackground(rb.isSelected() ? selectedColor : PANEL_COLOR);
                }
            });
        }
        bottomPanel.add(pnlOptions, BorderLayout.CENTER);

        // Panel nút
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15)); 
        pnlButtons.setBackground(BACKGROUND_COLOR);
        
        btnPrevious = new JButton("Câu trước");
        btnNext = new JButton("Câu tiếp theo");
        btnSubmit = new JButton("Nộp bài");
        btnExit = new JButton("Thoát");

        JButton[] buttons = {btnPrevious, btnNext, btnSubmit, btnExit};
        for (JButton btn : buttons) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
            btn.setFocusPainted(false);
            
            btn.setBackground(Color.WHITE); 
            btn.setForeground(BACKGROUND_COLOR.darker().darker()); 

            btn.setPreferredSize(new Dimension(160, 45));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        }

        // Tối ưu màu nút 
        Color btnPreviousNextColor = ACCENT_COLOR; 
        Color btnSubmitColor = PRIMARY_COLOR; 
        Color btnExitColor = DANGER_COLOR; 

        // Định nghĩa màu nền hover nhẹ hơn
        Color btnPreviousNextHoverBgColor = new Color(240, 230, 255); 
        Color btnSubmitHoverBgColor = new Color(230, 240, 255); 
        Color btnExitHoverBgColor = new Color(255, 230, 230); 
        
        // Gán màu chữ cụ thể
        btnPrevious.setForeground(btnPreviousNextColor.darker());
        btnNext.setForeground(btnPreviousNextColor.darker());
        btnSubmit.setForeground(btnSubmitColor.darker());
        btnExit.setForeground(btnExitColor.darker());

        // Cập nhật Border cho các nút
        Border borderPreviousNext = new LineBorder(btnPreviousNextColor, 2, true);
        Border borderSubmit = new LineBorder(btnSubmitColor, 2, true);
        Border borderExit = new LineBorder(btnExitColor, 2, true);

        btnPrevious.setBorder(BorderFactory.createCompoundBorder(borderPreviousNext, BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        btnNext.setBorder(BorderFactory.createCompoundBorder(borderPreviousNext, BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        btnSubmit.setBorder(BorderFactory.createCompoundBorder(borderSubmit, BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        btnExit.setBorder(BorderFactory.createCompoundBorder(borderExit, BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // Cập nhật Mouse Listener 
        btnPrevious.addMouseListener(createHoverListener(Color.WHITE, btnPreviousNextColor, btnPreviousNextHoverBgColor));
        btnNext.addMouseListener(createHoverListener(Color.WHITE, btnPreviousNextColor, btnPreviousNextHoverBgColor));
        btnSubmit.addMouseListener(createHoverListener(Color.WHITE, btnSubmitColor, btnSubmitHoverBgColor));
        btnExit.addMouseListener(createHoverListener(Color.WHITE, btnExitColor, btnExitHoverBgColor));
        
        pnlButtons.add(btnPrevious);
        pnlButtons.add(btnNext);
        pnlButtons.add(btnSubmit);
        pnlButtons.add(btnExit);
        bottomPanel.add(pnlButtons, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        btnPrevious.addActionListener(e -> previousQuestion());
        btnNext.addActionListener(e -> nextQuestion());
        btnSubmit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn nộp bài không?\nTổng thời gian đã sử dụng: " + (System.currentTimeMillis() - startTime)/1000 + " giây.",
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

        this.setVisible(false);
        this.setLocationRelativeTo(null); 
    }

    private MouseAdapter createHoverListener(Color normalBgColor, Color normalBorderColor, Color hoverBgColor) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton btn = ((JButton)e.getSource());
                btn.setBackground(hoverBgColor);
                // Viền đậm hơn một chút khi hover
                btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(normalBorderColor.darker(), 2, true),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                 JButton btn = ((JButton)e.getSource());
                btn.setBackground(normalBgColor);
                // Viền trở lại màu bình thường
                btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(normalBorderColor, 2, true),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
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
                        "<html><body style='font-family: Segoe UI; text-align: center; color: " + toHex(TEXT_COLOR) + "; background: " + toHex(PANEL_COLOR) + ";'>" +
                        "<h2 style='color: " + toHex(PRIMARY_COLOR) + ";'>HỆ THỐNG TRẮC NGHIỆM ONLINE</h2>" +
                        "<p>Chào mừng bạn!</p>" +
                        "<p style='margin-top: 15px;'><b>Bạn muốn làm gì?</b></p></body></html>",
                        "Hệ thống trắc nghiệm online",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE, 
                        null,
                        options,
                        options[0]
                    );
                    if (choice == -1) {
                        cleanupAndExit();
                        return;
                    }
                } else {
                    if (socket != null && !socket.isClosed()) {
                        try { socket.close(); } catch (IOException e) {}
                        oos = null;
                        ois = null;
                    }

                    socket = new Socket(SERVER_HOST, SERVER_PORT);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    LoginInfo loginInfo = showLoginDialog(choice == 1, lastUsername, lastPassword);

                    if (loginInfo == null) {
                        choice = -1; 
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
                        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) {}
                        continue;
                    } else {
                        authenticated = true;
                        currentUsername = loginInfo.username;
                        
                        String messageHtml = "<html><body style='font-family: Segoe UI; text-align: center; color: " + toHex(TEXT_COLOR) + "; background: " + toHex(PANEL_COLOR) + ";'>" +
                                "<p style='font-size: 14px;'>Chào mừng, <b>" + loginInfo.username + "</b>!</p>";
                        if (choice == 1) {
                            messageHtml += "<h3 style='color: " + toHex(new Color(60, 179, 113)) + ";'>ĐĂNG KÝ THÀNH CÔNG!</h3>" +
                                            "<p>Bạn có thể đăng nhập ngay bây giờ.</p>";
                        } else {
                            messageHtml += "<h3 style='color: " + toHex(PRIMARY_COLOR) + ";'>ĐĂNG NHẬP THÀNH CÔNG!</h3>" +
                                            "<p>Chuẩn bị làm bài...</p>";
                        }
                        messageHtml += "</body></html>";

                        // Tùy chỉnh OptionPane cho nền tối
                        UIManager.put("OptionPane.background", PANEL_COLOR);
                        UIManager.put("Panel.background", PANEL_COLOR);
                        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
                        JOptionPane.showMessageDialog(this, messageHtml, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        if (choice == 1) {
                            authenticated = false;
                            choice = 0; // Đẩy về Đăng nhập
                            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) {}
                            continue;
                        }
                    }
                }
            }
            
            // Khôi phục UIManager mặc định sau khi dùng Custom OptionPane
            UIManager.put("OptionPane.background", null);
            UIManager.put("Panel.background", null);
            UIManager.put("OptionPane.messageForeground", null);


            questions = (List<Question>) ois.readObject();
            
            // 🔥 PHẦN SỬA LỖI CHẤM ĐIỂM SAI: XÓA BỎ VIỆC XÁO TRỘN CÂU HỎI TRÊN CLIENT
            /*
            if (questions != null) {
                // Sử dụng Collections.shuffle để xáo trộn danh sách
                Collections.shuffle(questions); 
            }
            */
            // 🔥 HẾT PHẦN SỬA LỖI

            userAnswers = new Integer[questions.size()];
            for (int i = 0; i < userAnswers.length; i++) userAnswers[i] = -1;

            System.out.println("Client nhận được " + questions.size() + " câu hỏi từ server.");
            
            lblQuestionNumber.setText("Câu 1/" + questions.size());

            this.setVisible(true);
            this.setLocationRelativeTo(null);

            updateProgressBar();
            startQuiz();

        } catch (Exception e) {
            String errorMessage = "Không thể kết nối hoặc khởi động bài thi!\n\nVui lòng kiểm tra:\n- Server có đang chạy?\n- Lớp Quiz.Question đã implement Serializable và đồng bộ với Server?\n\nLỗi hệ thống: " + e.getClass().getName() + " - " + e.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi kết nối hoặc Khởi động", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            cleanupAndExit();
        }
    }
    
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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
        dialog.setSize(480, 400); 
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);

        JLabel titleLabel = new JLabel(isRegister ? "TẠO TÀI KHOẢN MỚI" : "ĐĂNG NHẬP HỆ THỐNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); 
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(titleLabel, gbc);
        
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        gbc.gridy = 1; gbc.insets = new Insets(5, 0, 15, 0);
        formPanel.add(separator, gbc);

        gbc.gridwidth = 1; gbc.gridy = 2; gbc.insets = new Insets(15, 15, 15, 5);
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST; 
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        usernameLabel.setForeground(TEXT_COLOR);
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBackground(PANEL_COLOR);
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setCaretColor(TEXT_COLOR); 
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10) 
        ));
        if (!lastUsername.isEmpty()) {
            usernameField.setText(lastUsername);
        }
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST; 
        formPanel.add(usernameField, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(15, 15, 15, 5);
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        passwordLabel.setForeground(TEXT_COLOR);
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBackground(PANEL_COLOR);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10) 
        ));
        if (!lastPassword.isEmpty()) {
            passwordField.setText(lastPassword);
        }
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(passwordField, gbc);

        if (isRegister) {
            JTextArea helpText = new JTextArea("- Tên đăng nhập: 3-20 ký tự\n- Mật khẩu: ít nhất 3 ký tự\n- Không được để trống");
            helpText.setEditable(false);
            helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            helpText.setBackground(BACKGROUND_COLOR);
            helpText.setForeground(new Color(150, 150, 150));
            helpText.setMargin(new Insets(10, 15, 0, 15));
            gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5, 15, 10, 15);
            formPanel.add(helpText, gbc);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton okButton = new JButton(isRegister ? "ĐĂNG KÝ" : "ĐĂNG NHẬP");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        // THAY ĐỔI: Áp dụng style nút mới cho dialog
        Color okButtonColor = PRIMARY_COLOR; // Màu chính: Xanh dương
        Color okButtonHoverBgColor = new Color(230, 240, 255); // Nền xanh nhạt hover
        okButton.setBackground(Color.WHITE); 
        okButton.setForeground(okButtonColor.darker());
        okButton.setFocusPainted(false);
        okButton.setPreferredSize(new Dimension(150, 45));
        Border okBorder = new LineBorder(okButtonColor, 2, true);
        okButton.setBorder(BorderFactory.createCompoundBorder(okBorder, BorderFactory.createEmptyBorder(10, 25, 10, 25)));


        JButton cancelButton = new JButton("HỦY");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        // THAY ĐỔI: Áp dụng style nút mới cho dialog
        Color cancelButtonColor = DANGER_COLOR; // Màu phụ: Đỏ
        Color cancelButtonHoverBgColor = new Color(255, 230, 230); // Nền đỏ nhạt hover
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(cancelButtonColor.darker());
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(150, 45));
        Border cancelBorder = new LineBorder(cancelButtonColor, 2, true);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(cancelBorder, BorderFactory.createEmptyBorder(10, 25, 10, 25)));
        
        // Áp dụng Mouse Listener mới 
        okButton.addMouseListener(createHoverListener(Color.WHITE, okButtonColor, okButtonHoverBgColor));
        cancelButton.addMouseListener(createHoverListener(Color.WHITE, cancelButtonColor, cancelButtonHoverBgColor));

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

        // Hiệu ứng Focus
        FocusListener focusBorderStyle = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        };

        usernameField.addFocusListener(focusBorderStyle);
        passwordField.addFocusListener(focusBorderStyle);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        usernameField.requestFocus();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        return result[0];
    }

    private void startQuiz() {
        // Tùy chỉnh OptionPane cho nền tối
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        JOptionPane.showMessageDialog(this,
            "<html><body style='font-family: Segoe UI; text-align: center; color: " + toHex(TEXT_COLOR) + "; background: " + toHex(PANEL_COLOR) + ";'>" +
            "<h3 style='color: " + toHex(PRIMARY_COLOR) + ";'>BẮT ĐẦU BÀI THI!</h3>" +
            "<p>Số câu hỏi: <b>" + questions.size() + " câu</b></p>" +
            "<p>Thời gian: <b>" + TIME_LIMIT_PER_QUESTION + " giây/câu</b></p>" +
            "<p>Bạn có thể quay lại câu trước. Nhấn 'Nộp bài' khi hoàn thành.</p>" +
            "<h4 style='color: " + toHex(new Color(60, 179, 113)) + ";'>CHÚC BẠN LÀM BÀI TỐT!</h4>" +
            "</body></html>",
            "Bắt đầu làm bài", JOptionPane.INFORMATION_MESSAGE);

        // Khôi phục UIManager mặc định
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageForeground", null);

        if (questions != null && !questions.isEmpty()) {
            startTime = System.currentTimeMillis(); 
            loadQuestion(0);
        } else {
            JOptionPane.showMessageDialog(this, "Không nhận được câu hỏi từ server!");
            cleanupAndExit();
        }
    }

    private void startTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        timeRemaining = TIME_LIMIT_PER_QUESTION;

        // Bổ sung thiết lập cho Progress Bar (thành thanh thời gian)
        progressBar.setMaximum(TIME_LIMIT_PER_QUESTION);
        progressBar.setValue(TIME_LIMIT_PER_QUESTION); 

        final Color INITIAL_TIMER_COLOR = DANGER_COLOR;
        lblTimer.setForeground(INITIAL_TIMER_COLOR);
        lblTimer.setBackground(PANEL_COLOR);
        lblTimer.setOpaque(true);

        timer = new Timer(1000, new ActionListener() {
            // Định nghĩa các màu sắc cho Progress Bar
            private final Color GOOD_COLOR = new Color(50, 205, 50); 
            private final Color WARNING_COLOR = new Color(255, 165, 0); 
            private final Color DANGER_COLOR_PB = new Color(255, 99, 71); 

            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));

                // Cập nhật Progress Bar
                progressBar.setValue(timeRemaining);

                // Logic đổi màu Progress Bar
                double ratio = (double)timeRemaining / TIME_LIMIT_PER_QUESTION;

                if (ratio > 0.4) {
                    progressBar.setForeground(GOOD_COLOR); 
                } else if (ratio > 0.15) {
                    progressBar.setForeground(WARNING_COLOR); 
                } else { 
                    progressBar.setForeground(DANGER_COLOR_PB); 
                }

                // Cảnh báo thời gian trên lblTimer
                if (timeRemaining <= 10) {
                    lblTimer.setForeground(WARNING_COLOR); 
                    if (timeRemaining <= 5) {
                        lblTimer.setForeground(DANGER_COLOR_PB); 
                        if (timeRemaining % 2 == 0) lblTimer.setBackground(PANEL_COLOR.darker());
                        else lblTimer.setBackground(new Color(60, 40, 40));
                    } else {
                        lblTimer.setBackground(PANEL_COLOR);
                    }
                } else {
                    lblTimer.setForeground(INITIAL_TIMER_COLOR);
                    lblTimer.setBackground(PANEL_COLOR);
                }

                if (timeRemaining <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(Quizclient.this,
                        "HẾT THỜI GIAN cho câu " + (currentIndex + 1) + "!\n\nChuyển sang câu tiếp theo.",
                        "Hết giờ", JOptionPane.WARNING_MESSAGE);
                    
                    saveCurrentAnswer();
                    
                    if (currentIndex < questions.size() - 1) {
                        currentIndex++;
                        loadQuestion(currentIndex); 
                    } else {
                        submitAnswers();
                    }
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
        progressBar.setString("Đã trả lời: " + answered + "/" + questions.size());
    }

    private void loadQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            if (!testCompleted) { 
                startTimer();
            }

            currentIndex = index;
            Question q = questions.get(index);
            lblQuestionNumber.setText("Câu " + (index + 1) + "/" + questions.size()); 
            txtQuestion.setText(q.getQuestionText());
            txtQuestion.setCaretPosition(0);
            List<String> opts = q.getOptions();
            bg.clearSelection();
            
            Color selectedColor = PRIMARY_COLOR.darker();
            
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText((char)('A' + i) + ". " + opts.get(i));
                boolean isSelected = userAnswers[index] != null && userAnswers[index] == i;
                rbOptions[i].setSelected(isSelected);
                rbOptions[i].setBackground(isSelected ? selectedColor : PANEL_COLOR);
                rbOptions[i].setEnabled(!testCompleted); 
                rbOptions[i].setForeground(TEXT_COLOR);
            }

            btnPrevious.setEnabled(currentIndex > 0 && !testCompleted);
            btnNext.setEnabled(currentIndex < questions.size() - 1 && !testCompleted);
            btnSubmit.setEnabled(!testCompleted);
            updateProgressBar();
            revalidate();
            repaint();
        }
    }
    
    // Thêm hàm chỉ cập nhật hiển thị câu hỏi (Dùng khi thoát chế độ xem đáp án)
    private void updateQuestionDisplayOnly(int index) {
         if (index >= 0 && index < questions.size()) {
            currentIndex = index;
            Question q = questions.get(index);
            
            lblQuestionNumber.setText("Câu " + (index + 1) + "/" + questions.size()); 
            txtQuestion.setText(q.getQuestionText());
            txtQuestion.setCaretPosition(0);
            List<String> opts = q.getOptions();
            bg.clearSelection();
            
            Color selectedColor = PRIMARY_COLOR.darker();
            
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText((char)('A' + i) + ". " + opts.get(i));
                boolean isSelected = userAnswers[index] != null && userAnswers[index] == i;
                rbOptions[i].setSelected(isSelected);
                rbOptions[i].setBackground(isSelected ? selectedColor : PANEL_COLOR);
                rbOptions[i].setEnabled(false); 
                rbOptions[i].setForeground(TEXT_COLOR);
            }

            btnPrevious.setEnabled(false);
            btnNext.setEnabled(false);
            btnSubmit.setEnabled(false);
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
        } else if (currentIndex == questions.size() - 1) {
             int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn đã hoàn thành tất cả các câu hỏi.\nBạn có muốn nộp bài luôn không?",
                "Hoàn thành bài thi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                submitAnswers();
            }
        }
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < 4; i++) {
            if (rbOptions[i].isSelected()) {
                userAnswers[currentIndex] = i;
                break;
            } else {
                userAnswers[currentIndex] = -1;
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

        btnPrevious.setEnabled(false);
        btnNext.setEnabled(false);
        btnSubmit.setEnabled(false);
        
        totalTimeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);

        try {
            saveCurrentAnswer();

            List<Integer> answersList = new ArrayList<>();
            for (Integer ans : userAnswers) {
                answersList.add(ans != null ? ans : -1);
            }

            oos.writeObject(answersList);
            oos.flush();

            int score = ois.readInt();
            finalScore = score; 

            try (FileWriter fw = new FileWriter("src/client_results.txt", true)) {
                double percentage = (double) score / questions.size() * 100;
                fw.write(currentUsername + "," + score + "," + questions.size() + "," + String.format("%.1f", percentage) + "," + totalTimeTaken + "\n");
            }
            showResults(score, totalTimeTaken);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResults(int score, int timeTaken) {
        double percentage = (double) score / questions.size() * 100;
        String grade;
        Color gradeColor;

        if (percentage >= 80) { grade = "Xuất sắc"; gradeColor = new Color(60, 179, 113); }
        else if (percentage >= 70) { grade = "Giỏi"; gradeColor = PRIMARY_COLOR; }
        else if (percentage >= 60) { grade = "Khá"; gradeColor = new Color(255, 140, 0); }
        else if (percentage >= 50) { grade = "Trung bình"; gradeColor = new Color(173, 216, 230).darker(); }
        else { grade = "Yếu"; gradeColor = DANGER_COLOR; }

        for (JRadioButton rb : rbOptions) {
            rb.setEnabled(false);
        }

        JDialog resultDialog = new JDialog(this, "KẾT QUẢ BÀI THI", true);
        resultDialog.setLayout(new BorderLayout(15, 15));
        resultDialog.setSize(500, 450); 
        resultDialog.setLocationRelativeTo(this);
        resultDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel(new GridLayout(6, 1, 5, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("HOÀN THÀNH BÀI THI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        contentPanel.add(titleLabel);
        
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        contentPanel.add(separator);

        JLabel scoreLabel = new JLabel("Điểm số: " + score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(TEXT_COLOR);
        contentPanel.add(scoreLabel);

        JLabel percentLabel = new JLabel("Tỷ lệ đúng: " + String.format("%.1f", percentage) + "%", SwingConstants.CENTER);
        percentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        percentLabel.setForeground(new Color(190, 190, 190));
        contentPanel.add(percentLabel);

        JLabel gradeLabel = new JLabel("Xếp loại: " + grade, SwingConstants.CENTER);
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gradeLabel.setForeground(gradeColor);
        contentPanel.add(gradeLabel);

        JLabel timeLabel = new JLabel("Tổng thời gian: " + timeTaken + " giây", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(ACCENT_COLOR);
        contentPanel.add(timeLabel);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Nút Bảng xếp hạng
        JButton rankingButton = new JButton("Bảng xếp hạng");
        rankingButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Color rankingColor = new Color(255, 165, 0).darker(); // Màu Cam đậm hơn
        Color rankingHoverBgColor = new Color(255, 230, 180); // Nền cam nhạt hover
        rankingButton.setBackground(Color.WHITE); 
        rankingButton.setForeground(rankingColor.darker());
        rankingButton.setFocusPainted(false);
        Border rankingBorder = new LineBorder(rankingColor, 2, true);
        rankingButton.setBorder(BorderFactory.createCompoundBorder(rankingBorder, BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        rankingButton.addMouseListener(createHoverListener(Color.WHITE, rankingColor, rankingHoverBgColor));
        rankingButton.addActionListener(e -> {
            resultDialog.setVisible(false);
            showRanking(resultDialog);
        });

        // Nút Xem đáp án
        JButton viewAnswersButton = new JButton("Xem đáp án");
        viewAnswersButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Color viewColor = PRIMARY_COLOR;
        Color viewHoverBgColor = new Color(230, 240, 255); // Nền xanh nhạt hover
        viewAnswersButton.setBackground(Color.WHITE);
        viewAnswersButton.setForeground(viewColor.darker());
        viewAnswersButton.setFocusPainted(false);
        Border viewBorder = new LineBorder(viewColor, 2, true);
        viewAnswersButton.setBorder(BorderFactory.createCompoundBorder(viewBorder, BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        viewAnswersButton.addMouseListener(createHoverListener(Color.WHITE, viewColor, viewHoverBgColor));
        viewAnswersButton.addActionListener(e -> {
            resultDialog.dispose(); // Đóng kết quả, chuyển sang chế độ xem đáp án
            verifyAnswers();
        });

        // Nút Thoát
        JButton closeButton = new JButton("Thoát");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Color closeColor = DANGER_COLOR;
        Color closeHoverBgColor = new Color(255, 230, 230); // Nền đỏ nhạt hover
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(closeColor.darker());
        closeButton.setFocusPainted(false);
        Border closeBorder = new LineBorder(closeColor, 2, true);
        closeButton.setBorder(BorderFactory.createCompoundBorder(closeBorder, BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        closeButton.addMouseListener(createHoverListener(Color.WHITE, closeColor, closeHoverBgColor));
        closeButton.addActionListener(e -> {
            resultDialog.dispose();
            cleanupAndExit();
        });

        buttonPanel.add(rankingButton);
        buttonPanel.add(viewAnswersButton);
        buttonPanel.add(closeButton);

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
        rankingDialog.setSize(750, 500); 
        rankingDialog.setLocationRelativeTo(this);
        rankingDialog.setLayout(new BorderLayout(10, 10));
        rankingDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("BẢNG XẾP HẠNG TOP PLAYERS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(255, 165, 0)); 
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel footerLabel = new JLabel("Xếp hạng dựa trên Điểm số (cao hơn) và Thời gian (ít hơn)", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        String[] columnNames = {"Hạng", "Tên người chơi", "Điểm số", "Tỷ lệ %", "Thời gian (giây)", "Xếp loại"};
        
        int displayLimit = Math.min(rankings.size(), 100); 
        String[][] data = new String[displayLimit][6];

        for (int i = 0; i < displayLimit; i++) {
            RankingEntry entry = rankings.get(i);
            
            String rankText;
            if (i == 0) rankText = "👑 Hạng 1";
            else if (i == 1) rankText = "🥈 Hạng 2";
            else if (i == 2) rankText = "🥉 Hạng 3";
            else rankText = String.valueOf(i + 1); 
            
            String grade = entry.percentage >= 80 ? "Xuất sắc" : entry.percentage >= 70 ? "Giỏi" : entry.percentage >= 60 ? "Khá" : entry.percentage >= 50 ? "Trung bình" : "Yếu";
            
            data[i][0] = rankText;
            data[i][1] = entry.username;
            data[i][2] = entry.score + "/" + entry.totalQuestions;
            data[i][3] = String.format("%.1f%%", entry.percentage);
            data[i][4] = String.valueOf(entry.timeTaken);
            data[i][5] = grade;
        }

        JTable table = new JTable(data, columnNames) {
             @Override
             public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                 Component c = super.prepareRenderer(renderer, row, column);
                 
                 c.setBackground(row % 2 == 0 ? PANEL_COLOR : new Color(50, 50, 75));
                 c.setForeground(TEXT_COLOR);
                 
                 if (isRowSelected(row)) {
                     c.setBackground(PRIMARY_COLOR.darker());
                 }

                 c.setFont(c.getFont().deriveFont(Font.PLAIN, 15f)); 

                 if (data[row][1].equals(currentUsername)) {
                     c.setFont(c.getFont().deriveFont(Font.BOLD | Font.ITALIC));
                     c.setForeground(new Color(60, 179, 113)); 
                 } 
                 
                 if (column == 4) {
                     c.setForeground(ACCENT_COLOR); 
                 }
                 if (column == 5) {
                     c.setForeground(gradeColor(data[row][5])); 
                 }

                 return c;
             }
         };

        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(PRIMARY_COLOR.darker()); 
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.setRowHeight(35); 
        table.setGridColor(BORDER_COLOR);
        table.setFocusTraversalKeysEnabled(false);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setSelectionBackground(PRIMARY_COLOR.darker()); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(table.getBackground());
        centerRenderer.setForeground(TEXT_COLOR);
        
        table.getColumnModel().getColumn(0).setCellRenderer(new RankCellRenderer()); 
        
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(100); 
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR); 

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton closeButton = new JButton("Đóng");

        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        Color closeColor = PRIMARY_COLOR;
        Color closeHoverBgColor = new Color(230, 240, 255); 
        closeButton.setBackground(Color.WHITE); 
        closeButton.setForeground(closeColor.darker());
        closeButton.setFocusPainted(false);
        Border closeBorder = new LineBorder(closeColor, 2, true);
        closeButton.setBorder(BorderFactory.createCompoundBorder(closeBorder, BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        closeButton.addMouseListener(createHoverListener(Color.WHITE, closeColor, closeHoverBgColor)); 
        closeButton.addActionListener(e -> {
            rankingDialog.dispose();
            resultDialog.setVisible(true);
        });

        buttonPanel.add(closeButton);

        rankingDialog.add(titleLabel, BorderLayout.NORTH);
        rankingDialog.add(scrollPane, BorderLayout.CENTER);
        rankingDialog.add(footerLabel, BorderLayout.SOUTH);
        rankingDialog.add(buttonPanel, BorderLayout.PAGE_END);
        
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
    
    // Hàm phụ trợ để lấy màu theo xếp loại
    private Color gradeColor(String grade) {
        if (grade.equals("Xuất sắc")) return new Color(60, 179, 113);
        if (grade.equals("Giỏi")) return PRIMARY_COLOR;
        if (grade.equals("Khá")) return new Color(255, 140, 0);
        if (grade.equals("Trung bình")) return new Color(173, 216, 230).darker();
        return DANGER_COLOR;
    }

    private void verifyAnswers() {
        if (!testCompleted) {
            JOptionPane.showMessageDialog(this, "Vui lòng nộp bài trước khi xem đáp án!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        btnPrevious.setEnabled(true); 
        btnNext.setEnabled(true);     
        btnSubmit.setEnabled(false);
        
        for (JRadioButton rb : rbOptions) {
            rb.setEnabled(false);
        }
        
        // Lưu lại các listeners cũ
        ActionListener[] nextListeners = btnNext.getActionListeners();
        ActionListener[] prevListeners = btnPrevious.getActionListeners();
        
        // Xóa listeners cũ
        for (ActionListener al : nextListeners) btnNext.removeActionListener(al);
        for (ActionListener al : prevListeners) btnPrevious.removeActionListener(al);

        
        JButton btnExitView = new JButton("Thoát chế độ xem đáp án");
        btnExitView.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        // THAY ĐỔI: Áp dụng style nút mới cho nút Thoát chế độ xem
        Color exitViewColor = DANGER_COLOR;
        Color exitViewHoverBgColor = new Color(255, 230, 230);
        btnExitView.setBackground(Color.WHITE);
        btnExitView.setForeground(exitViewColor.darker());
        btnExitView.setFocusPainted(false);
        Border exitViewBorder = new LineBorder(exitViewColor, 2, true);
        btnExitView.setBorder(BorderFactory.createCompoundBorder(exitViewBorder, BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        btnExitView.addMouseListener(createHoverListener(Color.WHITE, exitViewColor, exitViewHoverBgColor));

        
        JPanel pnlButtons = (JPanel) btnSubmit.getParent();
        
        pnlButtons.remove(btnSubmit);
        pnlButtons.add(btnExitView, SUBMIT_BUTTON_INDEX);
        
        if (timer != null) timer.stop();
        lblTimer.setText("ĐÃ HOÀN THÀNH");
        lblTimer.setForeground(new Color(60, 179, 113)); 
        lblTimer.setBackground(PANEL_COLOR);
        
        currentIndex = 0; 
        displayAnswerKey(currentIndex);
        
        ActionListener nextAction = e -> {
            currentIndex = (currentIndex + 1) % questions.size();
            displayAnswerKey(currentIndex);
        };
        
        ActionListener prevAction = e -> {
            currentIndex = (currentIndex - 1 + questions.size()) % questions.size();
            displayAnswerKey(currentIndex);
        };
        
        btnNext.addActionListener(nextAction);
        btnPrevious.addActionListener(prevAction);


        btnExitView.addActionListener(e -> {
            // 1. Khôi phục lại giao diện câu hỏi (trong trạng thái đã nộp bài)
            for (JRadioButton rb : rbOptions) {
                rb.setEnabled(false);
                rb.setBackground(PANEL_COLOR);
            }
            
            pnlButtons.remove(btnExitView);
            pnlButtons.add(btnSubmit, SUBMIT_BUTTON_INDEX);
            
            for (ActionListener al : btnNext.getActionListeners()) btnNext.removeActionListener(al);
            for (ActionListener al : btnPrevious.getActionListeners()) btnPrevious.removeActionListener(al);
            
            btnPrevious.setEnabled(false);
            btnNext.setEnabled(false);
            btnSubmit.setEnabled(false);
            
            updateQuestionDisplayOnly(currentIndex); 
            
            lblTimer.setText("ĐÃ HOÀN THÀNH");
            lblTimer.setForeground(new Color(60, 179, 113));
            lblTimer.setBackground(PANEL_COLOR);
            
            pnlButtons.revalidate();
            pnlButtons.repaint();
            
            // 2. Ẩn cửa sổ chính và hiển thị lại Dialog Kết quả
            Quizclient.this.setVisible(false);
            
            showResults(finalScore, totalTimeTaken); 
        });
        
        pnlButtons.revalidate();
        pnlButtons.repaint();
    }
    
    private void displayAnswerKey(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        currentIndex = index;
        Question q = questions.get(index);
        int correctAnswer = q.getCorrectAnswer();
        int userAnswer = userAnswers[index] != null ? userAnswers[index] : -1;
        
        lblQuestionNumber.setText("Xem đáp án: Câu " + (index + 1) + "/" + questions.size());
        txtQuestion.setText(q.getQuestionText());
        txtQuestion.setCaretPosition(0);
        
        List<String> opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            rbOptions[i].setText((char)('A' + i) + ". " + opts.get(i));
            rbOptions[i].setEnabled(false);
            rbOptions[i].setForeground(TEXT_COLOR);
            
            if (i == correctAnswer) {
                // Đáp án đúng (Màu xanh lá)
                rbOptions[i].setBackground(new Color(40, 80, 50)); 
            } else if (i == userAnswer) {
                // Đáp án sai của người dùng (Màu đỏ)
                rbOptions[i].setBackground(new Color(80, 40, 40)); 
            } else {
                // Các đáp án còn lại (Màu panel)
                rbOptions[i].setBackground(PANEL_COLOR);
            }
            rbOptions[i].setSelected(i == userAnswer || i == correctAnswer); 
        }
        
        String resultText = "";
        String correctOption = (char)('A' + correctAnswer) + ". " + opts.get(correctAnswer);

        if (userAnswer == correctAnswer) {
            resultText = "ĐÚNG. (Bạn đã chọn đáp án này: " + correctOption + ")";
        } else if (userAnswer != -1) {
            String userOption = (char)('A' + userAnswer) + ". " + opts.get(userAnswer);
            resultText = "SAI. (Bạn đã chọn: " + userOption + ")\nĐáp án đúng là: " + correctOption;
        } else {
            resultText = "CHƯA TRẢ LỜI. Đáp án đúng là: " + correctOption;
        }
        
        txtQuestion.setText(txtQuestion.getText() + "\n\n--- KẾT QUẢ CÂU TRẢ LỜI CỦA BẠN ---\n" + resultText);
        
        btnPrevious.setEnabled(true);
        btnNext.setEnabled(true);
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel.");
        }
        
        SwingUtilities.invokeLater(() -> {
            Quizclient client = new Quizclient();
            client.connectToServer();
        });
    }
}
