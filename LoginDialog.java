import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Module 6: LoginDialog
 * 
 * Provides interactive authentication dialog for CineBook users and administrators.
 * Verifies credentials directly against SQLite database using JDBC.
 */
public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private User authenticatedUser;

    public LoginDialog(Frame parent) {
        super(parent, "CineBook — Account Authentication", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(440, 500);
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                authenticatedUser = null;
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.COLOR_BG_LIGHT);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel titleLabel = new JLabel("CINEBOOK");
        titleLabel.setFont(UIConstants.FONT_HERO);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Cinema Admission Management • System Sign-In");
        subtitleLabel.setFont(UIConstants.FONT_SMALL);
        subtitleLabel.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Form
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(16, 24, 16, 24),
                BorderFactory.createCompoundBorder(
                        new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                        new EmptyBorder(18, 20, 18, 20)
                )
        ));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIConstants.FONT_BOLD);
        userLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(UIConstants.FONT_REGULAR);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIConstants.FONT_BOLD);
        passLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(UIConstants.FONT_REGULAR);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIConstants.FONT_SMALL);
        errorLabel.setForeground(UIConstants.COLOR_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Interactive Credential Quick-Fill Helpers
        JPanel hintBox = new JPanel();
        hintBox.setLayout(new BoxLayout(hintBox, BoxLayout.Y_AXIS));
        hintBox.setBackground(new Color(248, 250, 252));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        hintBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel hintTitle = new JLabel("Quick Login (Click to Fill - Password: 123):");
        hintTitle.setFont(UIConstants.FONT_BADGE);
        hintTitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JPanel quickFillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        quickFillRow.setOpaque(false);

        JButton fillAdminBtn = new JButton("admin");
        fillAdminBtn.setFont(UIConstants.FONT_SMALL);
        fillAdminBtn.setBackground(new Color(238, 242, 255));
        fillAdminBtn.setForeground(UIConstants.COLOR_ACCENT);
        fillAdminBtn.setFocusPainted(false);
        fillAdminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fillAdminBtn.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("123");
            errorLabel.setText(" ");
        });

        JButton fillUser1Btn = new JButton("user1");
        fillUser1Btn.setFont(UIConstants.FONT_SMALL);
        fillUser1Btn.setBackground(new Color(236, 253, 245));
        fillUser1Btn.setForeground(new Color(5, 150, 105));
        fillUser1Btn.setFocusPainted(false);
        fillUser1Btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fillUser1Btn.addActionListener(e -> {
            usernameField.setText("user1");
            passwordField.setText("123");
            errorLabel.setText(" ");
        });

        JButton fillUser2Btn = new JButton("user2");
        fillUser2Btn.setFont(UIConstants.FONT_SMALL);
        fillUser2Btn.setBackground(new Color(254, 243, 199));
        fillUser2Btn.setForeground(new Color(180, 83, 9));
        fillUser2Btn.setFocusPainted(false);
        fillUser2Btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fillUser2Btn.addActionListener(e -> {
            usernameField.setText("user2");
            passwordField.setText("123");
            errorLabel.setText(" ");
        });

        quickFillRow.add(fillAdminBtn);
        quickFillRow.add(fillUser1Btn);
        quickFillRow.add(fillUser2Btn);

        hintBox.add(hintTitle);
        hintBox.add(quickFillRow);

        formCard.add(userLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(errorLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(hintBox);

        mainPanel.add(formCard, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(UIConstants.COLOR_BG_LIGHT);

        JButton exitBtn = new JButton("Exit Application");
        exitBtn.setFont(UIConstants.FONT_REGULAR);
        exitBtn.setBackground(Color.WHITE);
        exitBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> {
            authenticatedUser = null;
            dispose();
        });

        JButton loginBtn = new JButton("Sign In  →");
        loginBtn.setFont(UIConstants.FONT_BOLD);
        loginBtn.setBackground(UIConstants.COLOR_ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        loginBtn.addActionListener(e -> attemptLogin());

        // Press Enter to trigger login
        getRootPane().setDefaultButton(loginBtn);

        buttonPanel.add(exitBtn);
        buttonPanel.add(loginBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = DatabaseManager.getInstance().authenticate(username, password);
        if (user != null) {
            this.authenticatedUser = user;
            BookingManager.getInstance().setCurrentUser(user);
            BookingManager.getInstance().logAudit("User logged in successfully: " + user.getName() + " (" + user.getRole() + ")");
            dispose();
        } else {
            errorLabel.setText("Invalid credentials. Please verify username and password.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    public User showLoginDialog() {
        setVisible(true);
        return authenticatedUser;
    }
}
