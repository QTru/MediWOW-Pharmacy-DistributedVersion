package presentation;

import core.dto.StaffDto;
import core.entities.enums.Role;
import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * MainMenuGui — Giao diện menu chính (placeholder).
 * Xác nhận đăng nhập thành công, hiển thị thông tin nhân viên,
 * đồng hồ thực và nút đăng xuất.
 */
public class MainMenuGui extends JFrame implements ActionListener {

    // ═══════════════════════════════════════════════════════════════════════════
    // Constants — Colors
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color CLR_SIDEBAR_BG = AppColors.DARK;
    private static final Color CLR_SIDEBAR_FG = AppColors.WHITE;
    private static final Color CLR_HEADER_BG  = AppColors.LIGHT;
    private static final Color CLR_CONTENT_BG = AppColors.BACKGROUND;
    private static final Color CLR_CARD_BG    = AppColors.WHITE;
    private static final Color CLR_ACCENT     = AppColors.DARK;
    private static final Color CLR_MUTED      = AppColors.PLACEHOLDER_TEXT;
    private static final Color CLR_SUCCESS    = AppColors.SUCCESS;
    private static final Color CLR_BTN_LOGOUT = AppColors.DANGER;
    private static final Color CLR_BTN_HOVER  = AppColors.MOMO;
    private static final Color CLR_BTN_FG     = AppColors.WHITE;
    private static final Color CLR_BORDER     = AppColors.LIGHT;

    // ═══════════════════════════════════════════════════════════════════════════
    // Constants — Fonts (minimum size: 16)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Font FONT_LOGO       = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_NAV        = new Font("Segoe UI", Font.BOLD,  16);
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  28);
    private static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_CARD_LABEL = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD,  16);
    private static final Font FONT_CLOCK      = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_FOOTER     = new Font("Segoe UI", Font.PLAIN, 16);

    // ═══════════════════════════════════════════════════════════════════════════
    // Components
    // ═══════════════════════════════════════════════════════════════════════════
    private JLabel  lblTime;
    private JButton btnLogout;

    // ═══════════════════════════════════════════════════════════════════════════
    // State
    // ═══════════════════════════════════════════════════════════════════════════
    private final NetworkService networkService;
    private final StaffDto       currentStaff;

    // ═══════════════════════════════════════════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════════════════════════════════════════
    public MainMenuGui(NetworkService networkService, StaffDto currentStaff) {
        this.networkService = networkService;
        this.currentStaff   = currentStaff;
        initComponents();
        setupWindowSettings();
        startClock();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI Initialization
    // ═══════════════════════════════════════════════════════════════════════════
    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CLR_CONTENT_BG);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Sidebar (Navigation) ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(CLR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Navigation buttons at top (placeholder — sẽ implement ở các bước tiếp theo)
        String[][] navItems = {
            { "Màn hình chính", "home",       "/icons/btn_home.png"      },
            { "Đơn hàng",       "sales",      "/icons/btn_selling.png"   },
            { "Sản phẩm",       "product",    "/icons/btn_product.png"   },
            { "Khuyến mại",     "promotion",  "/icons/btn_promotion.png" },
            { "Thống kê",       "statistics", "/icons/btn_statistic.png" },
            { "Nhân viên",      "staff",      "/icons/btn_staff.png"     },
        };

        boolean isManager = currentStaff.getRole() == Role.MANAGER;

        for (String[] item : navItems) {
            if (item[1].equals("staff") && !isManager) continue;
            sidebar.add(buildNavButton(item[0], item[2]));
        }

        // Spacer — đẩy Đăng xuất xuống cuối
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildDivider());

        // ── Logout button ─────────────────────────────────────────────────────
        btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(FONT_NAV);
        btnLogout.setForeground(CLR_BTN_FG);
        btnLogout.setBackground(CLR_BTN_LOGOUT);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnLogout.setBorder(new EmptyBorder(12, 16, 12, 16));
        btnLogout.setHorizontalAlignment(SwingConstants.CENTER);
        btnLogout.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnLogout.setIconTextGap(10);

        ImageIcon logoutIcon = loadIcon("/icons/btn_logout.png");
        if (logoutIcon != null)
            btnLogout.setIcon(scaleIcon(logoutIcon, 22));

        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btnLogout.setBackground(CLR_BTN_HOVER); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btnLogout.setBackground(CLR_BTN_LOGOUT); }
        });
        btnLogout.addActionListener(this);
        sidebar.add(btnLogout);

        return sidebar;
    }

    // ── Header (Logo + Clock + Greeting) ─────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_HEADER_BG);
        header.setBorder(new EmptyBorder(10, 20, 10, 24));

        // ── Left: Logo image + "MediWOW" brand ───────────────────────────────
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftPanel.setBackground(CLR_HEADER_BG);

        ImageIcon logoIcon = loadIcon("/images/logo.png");
        if (logoIcon != null) {
            JLabel lblLogoImg = new JLabel(new ImageIcon(logoIcon.getImage().getScaledInstance(120, 40, Image.SCALE_SMOOTH)));
            leftPanel.add(lblLogoImg);
        }

        header.add(leftPanel, BorderLayout.WEST);

        // ── Right: Clock + Greeting ───────────────────────────────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(CLR_HEADER_BG);

        lblTime = new JLabel();
        lblTime.setFont(FONT_CLOCK);
        lblTime.setForeground(CLR_ACCENT);
        rightPanel.add(lblTime);

        JLabel lblGreeting = new JLabel("Xin chào, " + currentStaff.getFullName() + "!");
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGreeting.setForeground(CLR_ACCENT);
        rightPanel.add(lblGreeting);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ── Content (Placeholder welcome card) ────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(CLR_CONTENT_BG);
        content.setBorder(new EmptyBorder(40, 40, 40, 40));

        content.add(buildWelcomeCard(), new GridBagConstraints());
        return content;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CLR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(48, 64, 48, 64)
        ));

        // Success icon
        JLabel lblIcon = new JLabel("✅");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(16));

        // Title
        JLabel lblTitle = new JLabel("Đăng nhập thành công!");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(CLR_ACCENT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));

        // Subtitle
        JLabel lblSubtitle = new JLabel("Hệ thống đang hoạt động. Chào mừng bạn đến với MediWOW.");
        lblSubtitle.setFont(FONT_SUBTITLE);
        lblSubtitle.setForeground(CLR_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(36));

        // ── Info cards ────────────────────────────────────────────────────────
        JPanel infoGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        infoGrid.setBackground(CLR_CARD_BG);
        infoGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoGrid.add(buildInfoCard("Nhân viên", currentStaff.getFullName()));
        infoGrid.add(buildInfoCard("Vai trò",
                currentStaff.getRole() == Role.MANAGER ? "Quản lý" : "Dược sĩ"));
        infoGrid.add(buildInfoCard("Tài khoản", currentStaff.getUsername()));
        card.add(infoGrid);
        card.add(Box.createVerticalStrut(36));

        // Note
        JLabel lblNote = new JLabel("Giao diện menu chính sẽ được hoàn thiện ở các bước tiếp theo.");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblNote.setForeground(CLR_MUTED);
        lblNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblNote);

        return card;
    }

    private JPanel buildInfoCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CLR_CONTENT_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(CLR_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(6));

        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(FONT_CARD_VALUE);
        val.setForeground(CLR_ACCENT);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(val);

        return card;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        footer.setBackground(CLR_HEADER_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, CLR_BORDER));

        JLabel lblFooter = new JLabel("© 2025 MediWOW — Hệ thống quản lý nhà thuốc");
        lblFooter.setFont(FONT_FOOTER);
        lblFooter.setForeground(CLR_MUTED);
        footer.add(lblFooter);
        return footer;
    }

    // ── Nav button factory ────────────────────────────────────────────────────
    private JButton buildNavButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_NAV);
        btn.setForeground(CLR_SIDEBAR_FG);
        btn.setBackground(CLR_SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);

        ImageIcon icon = loadIcon(iconPath);
        if (icon != null)
            btn.setIcon(scaleIcon(icon, 22));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(AppColors.PRIMARY); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(CLR_SIDEBAR_BG); }
        });
        // Placeholder — chưa có action (sẽ implement ở các bước tiếp theo)
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chức năng đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        return btn;
    }

    // ── Sidebar divider ───────────────────────────────────────────────────────
    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.PRIMARY);
        sep.setBackground(CLR_SIDEBAR_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Window settings ───────────────────────────────────────────────────────
    private void setupWindowSettings() {
        setTitle("MediWOW — Menu chính");
        setSize(1280, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmLogout();
            }
        });
    }

    // ── Real-time clock ───────────────────────────────────────────────────────
    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                "HH:mm:ss  EEEE, dd/MM/yyyy", Locale.of("vi", "VN"));
        Timer timer = new Timer(1000, e -> lblTime.setText(fmt.format(LocalDateTime.now())));
        timer.setInitialDelay(0);
        timer.start();
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

    // ── Scale an icon to a square size ───────────────────────────────────────
    private ImageIcon scaleIcon(ImageIcon icon, int size) {
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
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
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogout) {
            confirmLogout();
        }
    }

    // ── confirmLogout — hỏi xác nhận rồi quay về LoginGui ────────────────────
    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng xuất không?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginGui loginGui = new LoginGui(networkService);
                loginGui.setVisible(true);
            });
        }
    }
}
