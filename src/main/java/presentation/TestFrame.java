package presentation;

import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * TestFrame — Lớp giao diện riêng biệt.
 * Chỉ biết về NetworkService, không biết về socket/stream.
 */
public class TestFrame extends JFrame {

    // ─── Colors ──────────────────────────────────────────────────────────────
    private static final Color BG       = new Color(15, 17, 23);
    private static final Color SURFACE  = new Color(24, 27, 36);
    private static final Color ACCENT   = new Color(56, 189, 248);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color MUTED    = new Color(100, 116, 139);
    private static final Color SUCCESS  = new Color(34, 197, 94);
    private static final Color ERROR    = new Color(239, 68, 68);
    private static final Color BORDER   = new Color(30, 41, 59);

    // ─── Network ─────────────────────────────────────────────────────────────
    private final NetworkService networkService;

    // ─── Components ──────────────────────────────────────────────────────────
    private JLabel lblStatus;
    private JTextArea txtResult;
    private JButton btnPing;
    private JLabel lblConnectionStatus;

    public TestFrame(NetworkService networkService) {
        this.networkService = networkService;
        initUI();
        setupWindowListener();
    }

    // ─── Build UI ─────────────────────────────────────────────────────────────
    private void initUI() {
        setTitle("MediWOW — Pharmacy System");
        setSize(520, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // xử lý qua windowListener
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ─── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("MediWOW");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT);

        lblConnectionStatus = new JLabel("● Đã kết nối");
        lblConnectionStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblConnectionStatus.setForeground(SUCCESS);

        header.add(title, BorderLayout.WEST);
        header.add(lblConnectionStatus, BorderLayout.EAST);
        return header;
    }

    // ─── Center ───────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Button PING
        btnPing = new JButton("Gửi PING đến Server");
        btnPing.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnPing.setBackground(ACCENT);
        btnPing.setForeground(Color.WHITE);
        btnPing.setBorderPainted(false);
        btnPing.setFocusPainted(false);
        btnPing.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPing.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPing.setMaximumSize(new Dimension(220, 40));
        btnPing.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Hover effect
        btnPing.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnPing.setBackground(new Color(14, 165, 233));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnPing.setBackground(ACCENT);
            }
        });

        // ── Action: gửi request khi click ────────────────────────────────────
        btnPing.addActionListener(e -> onPingClicked());

        // Result area
        JLabel lblResultTitle = new JLabel("Phản hồi từ Server:");
        lblResultTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblResultTitle.setForeground(MUTED);
        lblResultTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblResultTitle.setBorder(new EmptyBorder(16, 0, 8, 0));

        txtResult = new JTextArea(5, 30);
        txtResult.setBackground(SURFACE);
        txtResult.setForeground(TEXT);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtResult.setBorder(new EmptyBorder(10, 12, 10, 12));
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(txtResult);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.getViewport().setBackground(SURFACE);

        center.add(btnPing);
        center.add(lblResultTitle);
        center.add(scroll);

        return center;
    }

    // ─── Footer / Status bar ──────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        footer.setBackground(SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(MUTED);
        footer.add(lblStatus);

        return footer;
    }

    // ─── Event handler ────────────────────────────────────────────────────────
    private void onPingClicked() {
        btnPing.setEnabled(false);
        setStatus("Đang gửi request...", MUTED);

        // Chạy trên worker thread để không block UI
        SwingWorker<Response, Void> worker = new SwingWorker<>() {
            @Override
            protected Response doInBackground() throws Exception {
                // Gửi request đến server
                return networkService.send(CommandType.PING);
            }

            @Override
            protected void done() {
                try {
                    Response response = get(); // lấy kết quả từ doInBackground
                    if (response.isSuccess()) {
                        txtResult.setText(
                                "✓ Thành công\n" +
                                        "Data: " + response.getData() + "\n" +
                                        "Message: " + response.getMessage()
                        );
                        setStatus("Nhận phản hồi thành công", SUCCESS);
                    } else {
                        txtResult.setText("✗ Thất bại\nMessage: " + response.getMessage());
                        setStatus("Server trả về lỗi", ERROR);
                    }
                } catch (Exception e) {
                    txtResult.setText("Lỗi kết nối: " + e.getMessage());
                    setStatus("Mất kết nối tới server", ERROR);
                    lblConnectionStatus.setText("● Mất kết nối");
                    lblConnectionStatus.setForeground(ERROR);
                } finally {
                    btnPing.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }

    // ─── Window listener — đóng socket khi tắt app ────────────────────────────
    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                networkService.disconnect();
                dispose();
                System.exit(0);
            }
        });
    }
}