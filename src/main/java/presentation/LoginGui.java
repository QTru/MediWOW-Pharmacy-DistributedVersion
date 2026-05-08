package presentation;

import core.dto.StaffDto;
import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Giao diện đăng nhập — thuần Java Swing, không sử dụng UI Designer.
 * Sử dụng SwingWorker để giao tiếp với server qua NetworkService.
 */
public class LoginGui extends JFrame implements ActionListener {

    // ═══════════════════════════════════════════════════════════════════════════
    // Constants — Colors & Fonts
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color CLR_BG_LEFT      = new Color(0xDD, 0xEF, 0xDD);   // #DDEFDD — xanh lá nhạt
    private static final Color CLR_BG_RIGHT     = Color.WHITE;
    private static final Color CLR_WELCOME      = new Color(0x0F, 0x37, 0x1B);   // #0F371B — xanh lá đậm
    private static final Color CLR_LABEL        = new Color(0x0F, 0x37, 0x1B);
    private static final Color CLR_FOOTER       = new Color(0x88, 0x88, 0x88);
    private static final Color CLR_BTN_LOGIN    = new Color(0x0F, 0x37, 0x1B);
    private static final Color CLR_BTN_HOVER    = new Color(0x1A, 0x5C, 0x2A);
    private static final Color CLR_BTN_FORGOT   = new Color(0x0F, 0x37, 0x1B);
    private static final Color CLR_BTN_FG       = new Color(0xFF, 0xFA, 0xEA);   // #FFFAEA

    private static final Font FONT_WELCOME      = new Font("Segoe UI", Font.BOLD,  36);
    private static final Font FONT_SUBWELCOME   = new Font("Segoe UI", Font.PLAIN, 20);
    private static final Font FONT_LABEL        = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_FIELD        = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_BUTTON       = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_FOOTER       = new Font("Segoe UI", Font.PLAIN, 14);

    // ═══════════════════════════════════════════════════════════════════════════
    // Components
    // ═══════════════════════════════════════════════════════════════════════════
    private JTextField     txtLogin;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnForgotPassword;
    private JLabel         lblStatus;

    // ═══════════════════════════════════════════════════════════════════════════
    // Network
    // ═══════════════════════════════════════════════════════════════════════════
    private final NetworkService networkService;

    // ═══════════════════════════════════════════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════════════════════════════════════════
    public LoginGui(NetworkService networkService) {
        this.networkService = networkService;
        initComponents();
        setupWindowSettings();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI Initialization
    // ═══════════════════════════════════════════════════════════════════════════
    private void initComponents() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setPreferredSize(new Dimension(1080, 600));
        setContentPane(root);

        root.add(buildLeftPanel());
        root.add(buildRightPanel());
    }

    // ── Left Panel (Logo + Banner) ────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_BG_LEFT);

        GridBagConstraints gbc = new GridBagConstraints();

        // Logo
        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(loadIcon("/images/logo.png"));
        lblLogo.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.ipadx = 200;
        panel.add(lblLogo, gbc);

        // Banner
        JLabel lblBanner = new JLabel();
        lblBanner.setIcon(loadIcon("/images/login_banner.png"));
        lblBanner.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 0;
        panel.add(lblBanner, gbc);

        return panel;
    }

    // ── Right Panel (Form) ────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_BG_RIGHT);
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // ── Header ───────────────────────────────────────────────────────────
        JLabel lblWelcome = new JLabel("Chào mừng trở lại!");
        lblWelcome.setFont(FONT_WELCOME);
        lblWelcome.setForeground(CLR_WELCOME);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblWelcome, gbc);

        JLabel lblSubWelcome = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSubWelcome.setFont(FONT_SUBWELCOME);
        lblSubWelcome.setForeground(CLR_WELCOME);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(lblSubWelcome, gbc);

        // ── Form Fields ───────────────────────────────────────────────────────
        JLabel lblLogin = new JLabel("Tên đăng nhập");
        lblLogin.setFont(FONT_LABEL);
        lblLogin.setForeground(CLR_LABEL);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblLogin, gbc);

        txtLogin = new JTextField();
        txtLogin.setFont(FONT_FIELD);
        txtLogin.setPreferredSize(new Dimension(0, 38));
        txtLogin.setToolTipText("Nhập tài khoản");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(txtLogin, gbc);

        JLabel lblPassword = new JLabel("Mật khẩu");
        lblPassword.setFont(FONT_LABEL);
        lblPassword.setForeground(CLR_LABEL);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(FONT_FIELD);
        txtPassword.setPreferredSize(new Dimension(0, 38));
        txtPassword.setToolTipText("Nhập mật khẩu");
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(txtPassword, gbc);

        // ── Status label (error/info messages) ────────────────────────────────
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(new Color(0xCC, 0x00, 0x00));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(lblStatus, gbc);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel pnlButton = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlButton.setBackground(CLR_BG_RIGHT);

        btnLogin = buildButton("Đăng nhập", CLR_BTN_LOGIN, CLR_BTN_HOVER, CLR_BTN_FG);
        btnForgotPassword = buildButton("Quên mật khẩu", CLR_BTN_FORGOT, CLR_BTN_HOVER, CLR_BTN_FG);
        pnlButton.add(btnLogin);
        pnlButton.add(btnForgotPassword);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(pnlButton, gbc);

        // ── Footer ────────────────────────────────────────────────────────────
        JLabel lblFooter = new JLabel("© 2025 MediWOW");
        lblFooter.setFont(FONT_FOOTER);
        lblFooter.setForeground(CLR_FOOTER);
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8;
        gbc.insets = new Insets(60, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblFooter, gbc);

        // ── Accessibility ─────────────────────────────────────────────────────
        lblLogin.setLabelFor(txtLogin);
        lblPassword.setLabelFor(txtPassword);

        // ── Listeners ─────────────────────────────────────────────────────────
        btnLogin.addActionListener(this);
        btnForgotPassword.addActionListener(this);
        // Enter trên txtLogin → focus sang password
        txtLogin.addActionListener(e -> txtPassword.requestFocus());
        // Enter trên txtPassword → submit
        txtPassword.addActionListener(this);

        return panel;
    }

    // ── Button factory ────────────────────────────────────────────────────────
    private JButton buildButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 48));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Window settings ───────────────────────────────────────────────────────
    private void setupWindowSettings() {
        setTitle("MediWOW — Đăng nhập");
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // ── Load image safely (returns null if not found) ─────────────────────────
    private ImageIcon loadIcon(String path) {
        try {
            var url = getClass().getResource(path);
            return url != null ? new ImageIcon(url) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Async Helper — SwingWorker
    // ═══════════════════════════════════════════════════════════════════════════
    private void sendAsync(CommandType command, Object data,
                           Consumer<Response> onSuccess, Consumer<Exception> onError) {
        SwingWorker<Response, Void> worker = new SwingWorker<>() {
            @Override
            protected Response doInBackground() throws Exception {
                return networkService.send(command, data);
            }
            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        };
        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Event Handlers
    // ═══════════════════════════════════════════════════════════════════════════

    // ── actionPerformed — routes events to specific handlers ──────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnLogin || src == txtPassword) {
            handleLogin();
        } else if (src == btnForgotPassword) {
            handleForgotPassword();
        }
    }

    // ── handleLogin ───────────────────────────────────────────────────────────
    private void handleLogin() {
        String username = txtLogin.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Validate input
        if (username.isEmpty()) {
            showStatus("Vui lòng nhập tên đăng nhập");
            txtLogin.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showStatus("Vui lòng nhập mật khẩu");
            txtPassword.requestFocus();
            return;
        }

        // Disable UI while loading
        setLoginLoading(true);
        lblStatus.setText(" ");

        sendAsync(
            CommandType.STAFF_LOGIN,
            new String[]{ username, password },
            // onSuccess
            response -> {
                setLoginLoading(false);
                if (response.isSuccess()) {
                    StaffDto staff = (StaffDto) response.getData();
                    openMainMenu(staff);
                } else {
                    showStatus(response.getMessage());
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
            },
            // onError
            error -> {
                setLoginLoading(false);
                showStatus("Lỗi kết nối: " + error.getMessage());
            }
        );
    }

    // ── handleForgotPassword ──────────────────────────────────────────────────
    private void handleForgotPassword() {
        JOptionPane.showMessageDialog(
                this,
                "Chức năng quên mật khẩu chưa được triển khai.\nVui lòng liên hệ quản lý hệ thống.",
                "Quên mật khẩu",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════════════════════

    // ── openMainMenu — chuyển sang MainMenuGui sau khi đăng nhập thành công ───
    private void openMainMenu(StaffDto staff) {
        dispose(); // Đóng cửa sổ đăng nhập
        SwingUtilities.invokeLater(() -> {
            MainMenuGui mainMenu = new MainMenuGui(networkService, staff);
            mainMenu.setVisible(true);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI Helpers
    // ═══════════════════════════════════════════════════════════════════════════
    private void setLoginLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnForgotPassword.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    private void showStatus(String message) {
        lblStatus.setText(message);
    }
}
