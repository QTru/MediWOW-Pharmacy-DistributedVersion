package presentation;

import core.dto.StaffDto;
import core.entities.enums.Role;
import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class StaffGui extends JPanel {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color CLR_BG        = AppColors.BACKGROUND;
    private static final Color CLR_WHITE     = AppColors.WHITE;
    private static final Color CLR_ACCENT    = AppColors.DARK;
    private static final Color CLR_PRIMARY   = AppColors.PRIMARY;
    private static final Color CLR_MUTED     = AppColors.PLACEHOLDER_TEXT;
    private static final Color CLR_BORDER    = AppColors.LIGHT;
    private static final Color CLR_SUCCESS   = AppColors.SUCCESS;
    private static final Color CLR_DANGER    = AppColors.DANGER;
    private static final Color CLR_WARNING   = AppColors.WARNING;
    private static final Color CLR_HEADER_BG = AppColors.DARK;
    private static final Color CLR_ROW_ODD   = AppColors.WHITE;
    private static final Color CLR_ROW_EVEN  = new Color(0xEEF7FA);
    private static final Color CLR_ROW_SEL   = new Color(0xB3E0F0);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD,  16);
    private static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_VALUE      = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_SEARCH     = new Font("Segoe UI", Font.PLAIN, 14);

    // ── Table columns ─────────────────────────────────────────────────────────
    private static final String[] STAFF_COLS = {
            "Mã NV", "Họ tên", "Vai trò", "Số điện thoại", "Email", "Ngày vào làm", "Trạng thái"
    };

    // ── Toolbar components ────────────────────────────────────────────────────
    private JTextField tfSearch;
    private JButton    btnSearch;
    private JButton    btnReload;
    private JButton    btnAdd;
    private JButton    btnEdit;

    // ── Staff table ───────────────────────────────────────────────────────────
    private JTable            tblStaff;
    private DefaultTableModel mdlStaff;
    private TableRowSorter<DefaultTableModel> sorterStaff;

    // ── Detail panel ──────────────────────────────────────────────────────────
    private JPanel pnlDetail;

    // ── State ─────────────────────────────────────────────────────────────────
    private final NetworkService networkService;
    private List<StaffDto>       staffList = new ArrayList<>();
    private StaffDto             selectedStaff;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════════════════
    // Constructor
    // ═════════════════════════════════════════════════════════════════════════
    public StaffGui(NetworkService networkService) {
        this.networkService = networkService;
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);
        initComponents();
        loadStaffs();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UI Initialization
    // ═════════════════════════════════════════════════════════════════════════
    private void initComponents() {
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(CLR_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, CLR_BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel lblTitle = new JLabel("Quản lý Nhân viên");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(CLR_ACCENT);
        bar.add(lblTitle, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(CLR_WHITE);

        tfSearch = new JTextField(20);
        tfSearch.setFont(FONT_SEARCH);
        tfSearch.setToolTipText("Tìm theo tên, mã NV, email...");
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        tfSearch.addActionListener(e -> applySearch());
        toolbar.add(tfSearch);

        btnSearch = makeBtn("Tìm", CLR_PRIMARY);
        btnSearch.addActionListener(e -> applySearch());
        toolbar.add(btnSearch);

        btnReload = makeBtn("Tải lại", CLR_ACCENT);
        btnReload.addActionListener(e -> loadStaffs());
        toolbar.add(btnReload);

        btnAdd = makeBtn("+ Thêm", CLR_SUCCESS);
        btnAdd.addActionListener(e -> openAddDialog());
        toolbar.add(btnAdd);

        btnEdit = makeBtn("✎ Sửa", CLR_WARNING);
        btnEdit.setForeground(Color.BLACK);
        btnEdit.addActionListener(e -> openEditDialog());
        toolbar.add(btnEdit);

        bar.add(toolbar, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildMainArea() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildDetailPanel());
        split.setDividerLocation(620);
        split.setDividerSize(5);
        split.setContinuousLayout(true);
        split.setBorder(null);
        return split;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 6));

        mdlStaff = new DefaultTableModel(STAFF_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStaff = new JTable(mdlStaff);
        styleTable(tblStaff);

        sorterStaff = new TableRowSorter<>(mdlStaff);
        tblStaff.setRowSorter(sorterStaff);

        tblStaff.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onStaffSelected();
        });

        JScrollPane scroll = new JScrollPane(tblStaff);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JLabel lblCount = new JLabel("0 nhân viên");
        lblCount.setFont(FONT_VALUE);
        lblCount.setForeground(CLR_MUTED);
        lblCount.setBorder(new EmptyBorder(6, 4, 0, 0));
        panel.add(lblCount, BorderLayout.SOUTH);

        mdlStaff.addTableModelListener(e ->
                lblCount.setText(mdlStaff.getRowCount() + " nhân viên"));

        return panel;
    }

    private JPanel buildDetailPanel() {
        pnlDetail = new JPanel(new BorderLayout());
        pnlDetail.setBackground(CLR_BG);
        pnlDetail.setBorder(new EmptyBorder(12, 6, 12, 12));
        pnlDetail.add(buildPlaceholderDetail(), BorderLayout.CENTER);
        return pnlDetail;
    }

    private JPanel buildPlaceholderDetail() {
        JPanel ph = new JPanel(new GridBagLayout());
        ph.setBackground(CLR_WHITE);
        ph.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1, true));
        JLabel lbl = new JLabel("← Chọn một nhân viên để xem chi tiết");
        lbl.setFont(FONT_VALUE);
        lbl.setForeground(CLR_MUTED);
        ph.add(lbl);
        return ph;
    }

    // ── Detail content ────────────────────────────────────────────────────────
    private JPanel buildDetailContent(StaffDto s) {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(CLR_BG);

        // ── Card: thông tin cơ bản ─────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 6, 4, 6);
        gc.anchor  = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 4;
        card.add(sectionLabel("Thông tin nhân viên"), gc);
        gc.gridwidth = 1;
        gc.gridy++;

        int[] row = {gc.gridy};
        addDetailRow(card, gc, row, "Mã NV:",          s.getId());
        addDetailRow(card, gc, row, "Họ tên:",         s.getFullName());
        addDetailRow(card, gc, row, "Username:",       s.getUsername());
        addDetailRow(card, gc, row, "Vai trò:",        roleLabel(s.getRole()));
        addDetailRow(card, gc, row, "Số giấy phép:",   s.getLicenseNumber());
        addDetailRow(card, gc, row, "Số điện thoại:",  s.getPhoneNumber());
        addDetailRow(card, gc, row, "Email:",          s.getEmail());
        addDetailRow(card, gc, row, "Ngày vào làm:",
                s.getHireDate() != null ? s.getHireDate().format(DATE_FMT) : "—");
        addDetailRow(card, gc, row, "Trạng thái:",
                s.isActive() ? "✔ Đang hoạt động" : "✕ Vô hiệu hóa");

        root.add(card, BorderLayout.NORTH);
        return root;
    }

    private void addDetailRow(JPanel card, GridBagConstraints gc,
                              int[] rowRef, String label, String value) {
        gc.gridx = 0; gc.gridy = rowRef[0];
        gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_MUTED);
        card.add(lbl, gc);

        gc.gridx = 1; gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(FONT_VALUE);
        val.setForeground(CLR_ACCENT);
        card.add(val, gc);
        rowRef[0]++;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Table styling
    // ═════════════════════════════════════════════════════════════════════════
    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(CLR_ROW_SEL);
        table.setSelectionForeground(CLR_ACCENT);
        table.setFocusable(false);

        table.getTableHeader().setFont(FONT_TABLE_HDR);
        table.getTableHeader().setBackground(CLR_HEADER_BG);
        table.getTableHeader().setForeground(AppColors.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(new EmptyBorder(4, 10, 4, 10));
                if (sel) {
                    setBackground(CLR_ROW_SEL);
                    setForeground(CLR_ACCENT);
                } else {
                    setBackground(row % 2 == 0 ? CLR_ROW_ODD : CLR_ROW_EVEN);
                    setForeground(CLR_ACCENT);
                }
                return this;
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Data Loading
    // ═════════════════════════════════════════════════════════════════════════
    private void loadStaffs() {
        setLoading(true);
        sendAsync(CommandType.STAFF_LOAD_ALL, null,
                resp -> SwingUtilities.invokeLater(() -> {
                    setLoading(false);

                    if (resp == null) {
                        showError("Server không phản hồi.");
                        return;
                    }
                    if (!resp.isSuccess()) {
                        showError("Server báo lỗi: " + resp.getMessage());
                        return;
                    }

                    Object raw = resp.getData();
                    if (!(raw instanceof List<?> list)) {
                        showError("Dữ liệu trả về không hợp lệ (type: "
                                + (raw == null ? "null" : raw.getClass().getName()) + ")");
                        return;
                    }

                    staffList.clear();
                    mdlStaff.setRowCount(0);
                    for (Object obj : list) {
                        if (obj instanceof StaffDto dto) {
                            staffList.add(dto);
                            mdlStaff.addRow(new Object[]{
                                    dto.getId(), dto.getFullName(), roleLabel(dto.getRole()),
                                    dto.getPhoneNumber(), dto.getEmail(),
                                    dto.getHireDate() != null
                                            ? dto.getHireDate().format(DATE_FMT) : "—",
                                    dto.isActive() ? "✔ Đang hoạt động" : "✕ Vô hiệu hóa"
                            });
                        }
                    }
                }),
                ex -> SwingUtilities.invokeLater(() -> {
                    setLoading(false);
                    showError("Lỗi kết nối: " + ex.getMessage());
                })
        );
    }

    private void applySearch() {
        String keyword = tfSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorterStaff.setRowFilter(null);
        } else {
            sorterStaff.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Selection handler
    // ═════════════════════════════════════════════════════════════════════════
    private void onStaffSelected() {
        int viewRow = tblStaff.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = tblStaff.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= staffList.size()) return;

        selectedStaff = staffList.get(modelRow);
        refreshDetailPanel(selectedStaff);
    }

    private void refreshDetailPanel(StaffDto s) {
        pnlDetail.removeAll();
        JScrollPane scroll = new JScrollPane(buildDetailContent(s));
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pnlDetail.add(scroll, BorderLayout.CENTER);
        pnlDetail.revalidate();
        pnlDetail.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Add / Edit dialogs
    // ═════════════════════════════════════════════════════════════════════════
    private void openAddDialog() {
        StaffFormDialog dialog = new StaffFormDialog(
                getParentFrame(), networkService, null,
                saved -> {
                    staffList.add(saved);
                    mdlStaff.addRow(new Object[]{
                            saved.getId(), saved.getFullName(), roleLabel(saved.getRole()),
                            saved.getPhoneNumber(), saved.getEmail(),
                            saved.getHireDate() != null
                                    ? saved.getHireDate().format(DATE_FMT) : "—",
                            saved.isActive() ? "✔ Đang hoạt động" : "✕ Vô hiệu hóa"
                    });
                }
        );
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedStaff == null) {
            showWarn("Vui lòng chọn nhân viên cần sửa.");
            return;
        }
        StaffFormDialog dialog = new StaffFormDialog(
                getParentFrame(), networkService, selectedStaff,
                updated -> {
                    int idx = staffList.indexOf(selectedStaff);
                    if (idx >= 0) {
                        staffList.set(idx, updated);
                        mdlStaff.setValueAt(updated.getId(),                      idx, 0);
                        mdlStaff.setValueAt(updated.getFullName(),                idx, 1);
                        mdlStaff.setValueAt(roleLabel(updated.getRole()),         idx, 2);
                        mdlStaff.setValueAt(updated.getPhoneNumber(),             idx, 3);
                        mdlStaff.setValueAt(updated.getEmail(),                   idx, 4);
                        mdlStaff.setValueAt(updated.getHireDate() != null
                                ? updated.getHireDate().format(DATE_FMT) : "—",  idx, 5);
                        mdlStaff.setValueAt(updated.isActive()
                                ? "✔ Đang hoạt động" : "✕ Vô hiệu hóa",         idx, 6);
                    }
                    selectedStaff = updated;
                    refreshDetailPanel(updated);
                }
        );
        dialog.setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utilities
    // ═════════════════════════════════════════════════════════════════════════
    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(AppColors.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addMouseListener(new MouseAdapter() {
            final Color orig = bg;
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(orig.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(orig); }
        });
        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(CLR_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private String roleLabel(Role role) {
        if (role == null) return "—";
        return switch (role) {
            case PHARMACIST -> "Dược sĩ";
            case MANAGER    -> "Quản lý";
        };
    }

    private void setLoading(boolean loading) {
        if (btnReload == null) return;
        btnReload.setEnabled(!loading);
        btnReload.setText(loading ? "Đang tải..." : "Tải lại");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private JFrame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return w instanceof JFrame f ? f : null;
    }

    private void sendAsync(CommandType cmd, Object data,
                           Consumer<Response> onSuccess,
                           Consumer<Exception> onError) {
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception {
                return networkService.send(cmd, data);
            }
            @Override protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    onError.accept(cause instanceof Exception ex ? ex
                            : new RuntimeException(cause));
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Inner class: StaffFormDialog
    // ═════════════════════════════════════════════════════════════════════════
    static class StaffFormDialog extends JDialog {

        private static final Color D_ACCENT  = AppColors.DARK;
        private static final Color D_BG      = AppColors.BACKGROUND;
        private static final Color D_WHITE   = AppColors.WHITE;
        private static final Color D_BORDER  = AppColors.LIGHT;
        private static final Color D_PRIMARY = AppColors.PRIMARY;
        private static final Color D_SUCCESS = AppColors.SUCCESS;
        private static final Color D_DANGER  = AppColors.DANGER;

        private static final Font FD_LABEL = new Font("Segoe UI", Font.BOLD,  14);
        private static final Font FD_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font FD_BTN   = new Font("Segoe UI", Font.BOLD,  14);
        private static final Font FD_TITLE = new Font("Segoe UI", Font.BOLD,  18);
        private static final Font FD_SEC   = new Font("Segoe UI", Font.BOLD,  15);

        private static final DateTimeFormatter DATE_FMT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ── Form fields ───────────────────────────────────────────────────────
        private JTextField        tfUsername;
        private JPasswordField    pfPassword;       // chỉ hiện khi tạo mới
        private JTextField        tfFullName;
        private JTextField        tfLicense;
        private JTextField        tfPhone;
        private JTextField        tfEmail;
        private JComboBox<String> cbRole;
        private JCheckBox         chkActive;
        private JTextField        tfHireDate;

        // ── State ─────────────────────────────────────────────────────────────
        private final NetworkService     networkService;
        private final StaffDto           editTarget;
        private final Consumer<StaffDto> onSaved;
        private JButton                  btnSave;

        StaffFormDialog(JFrame parent, NetworkService ns,
                        StaffDto editTarget,
                        Consumer<StaffDto> onSaved) {
            super(parent,
                    editTarget == null ? "Thêm nhân viên mới" : "Sửa nhân viên",
                    true);
            this.networkService = ns;
            this.editTarget     = editTarget;
            this.onSaved        = onSaved;
            initDialog();
            if (editTarget != null) fillForm(editTarget);
        }

        private void initDialog() {
            setSize(660, editTarget == null ? 480 : 460);
            setMinimumSize(new Dimension(560, 420));
            setLocationRelativeTo(getOwner());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(D_BG);
            getContentPane().setLayout(new BorderLayout(0, 0));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(D_ACCENT);
            header.setBorder(new EmptyBorder(14, 20, 14, 20));
            JLabel lblTitle = new JLabel(
                    editTarget == null ? "Thêm nhân viên mới" : "Sửa nhân viên");
            lblTitle.setFont(FD_TITLE);
            lblTitle.setForeground(AppColors.WHITE);
            header.add(lblTitle, BorderLayout.WEST);
            getContentPane().add(header, BorderLayout.NORTH);

            getContentPane().add(buildFormScroll(), BorderLayout.CENTER);
            getContentPane().add(buildFooter(),     BorderLayout.SOUTH);
        }

        // ── Form ──────────────────────────────────────────────────────────────
        private JScrollPane buildFormScroll() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(D_WHITE);
            panel.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets  = new Insets(6, 6, 6, 6);
            gc.anchor  = GridBagConstraints.WEST;
            gc.fill    = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // ── Section: Tài khoản ─────────────────────────────────────────
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(secLabel("Thông tin tài khoản"), gc);
            gc.gridwidth = 1; row++;

            tfUsername = field();
            if (editTarget == null) {
                // Tạo mới: có thêm trường mật khẩu
                pfPassword = new JPasswordField();
                pfPassword.setFont(FD_INPUT);
                pfPassword.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(D_BORDER, 1, true),
                        new EmptyBorder(6, 8, 6, 8)
                ));
                addFormRow(panel, gc, row++, "Username *", tfUsername,
                        "Mật khẩu *", pfPassword);
            } else {
                // Sửa: không cho đổi password qua form này
                addFormRowSingle(panel, gc, row++, "Username *", tfUsername);
            }

            // ── Section: Thông tin cá nhân ─────────────────────────────────
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(secLabel("Thông tin cá nhân"), gc);
            gc.gridwidth = 1; row++;

            tfFullName = field();
            tfLicense  = field();
            addFormRow(panel, gc, row++, "Họ tên *", tfFullName,
                    "Số giấy phép *", tfLicense);

            tfPhone = field();
            tfEmail = field();
            addFormRow(panel, gc, row++, "Số điện thoại *", tfPhone,
                    "Email *", tfEmail);

            // ── Section: Vai trò & trạng thái ─────────────────────────────
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(secLabel("Vai trò & Trạng thái"), gc);
            gc.gridwidth = 1; row++;

            cbRole = new JComboBox<>(new String[]{ "Dược sĩ", "Quản lý" });
            cbRole.setFont(FD_INPUT);
            cbRole.setBackground(D_WHITE);

            tfHireDate = field();
            tfHireDate.setToolTipText("Định dạng: dd/MM/yyyy HH:mm (để trống = thời điểm hiện tại)");
            addFormRow(panel, gc, row++, "Vai trò *", cbRole,
                    "Ngày vào làm", tfHireDate);

            chkActive = new JCheckBox("Đang hoạt động");
            chkActive.setBackground(D_WHITE);
            chkActive.setFont(FD_INPUT);
            chkActive.setSelected(true);

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1;
            panel.add(chkActive, gc);
            gc.gridwidth = 1; row++;

            // Spacer
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
            gc.fill = GridBagConstraints.VERTICAL; gc.weighty = 1;
            panel.add(new JPanel(), gc);

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(12);
            return scroll;
        }

        private JPanel buildFooter() {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(D_BG);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, D_BORDER));

            JButton btnCancel = dlgBtn("Hủy", D_DANGER);
            btnSave = dlgBtn(editTarget == null ? "Lưu nhân viên" : "Cập nhật", D_SUCCESS);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> saveStaff());

            footer.add(btnCancel);
            footer.add(btnSave);
            return footer;
        }

        // ── Fill existing data ────────────────────────────────────────────────
        private void fillForm(StaffDto s) {
            tfUsername.setText(nvl(s.getUsername()));
            tfFullName.setText(nvl(s.getFullName()));
            tfLicense.setText(nvl(s.getLicenseNumber()));
            tfPhone.setText(nvl(s.getPhoneNumber()));
            tfEmail.setText(nvl(s.getEmail()));
            chkActive.setSelected(s.isActive());

            if (s.getRole() != null)
                cbRole.setSelectedIndex(switch (s.getRole()) {
                    case PHARMACIST -> 0;
                    case MANAGER    -> 1;
                });

            if (s.getHireDate() != null)
                tfHireDate.setText(s.getHireDate().format(DATE_FMT));
        }

        // ── Save ──────────────────────────────────────────────────────────────
        private void saveStaff() {
            // ── Validate trường bắt buộc ──────────────────────────────────────
            if (tfUsername.getText().isBlank()
                    || tfFullName.getText().isBlank()
                    || tfLicense.getText().isBlank()
                    || tfPhone.getText().isBlank()
                    || tfEmail.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đầy đủ các trường bắt buộc (*).",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validate email ────────────────────────────────────────────────
            String emailVal = tfEmail.getText().trim();
            if (!emailVal.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this,
                        "Email không đúng định dạng (ví dụ: ten@example.com).",
                        "Email không hợp lệ", JOptionPane.WARNING_MESSAGE);
                tfEmail.requestFocus();
                return;
            }

            // ── Validate số điện thoại (7–15 chữ số, cho phép dấu +, -, khoảng trắng) ──
            String phoneVal = tfPhone.getText().trim();
            if (!phoneVal.matches("^[+]?[\\d\\s\\-]{7,15}$")) {
                JOptionPane.showMessageDialog(this,
                        "Số điện thoại không hợp lệ (7–15 chữ số, ví dụ: 0901234567).",
                        "Số điện thoại không hợp lệ", JOptionPane.WARNING_MESSAGE);
                tfPhone.requestFocus();
                return;
            }

            // ── Validate password khi tạo mới ────────────────────────────────
            String passwordVal = "";
            if (editTarget == null) {
                passwordVal = new String(pfPassword.getPassword()).trim();
                if (passwordVal.isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng nhập mật khẩu.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    pfPassword.requestFocus();
                    return;
                }
                if (passwordVal.length() < 6) {
                    JOptionPane.showMessageDialog(this,
                            "Mật khẩu phải có ít nhất 6 ký tự.",
                            "Mật khẩu quá ngắn", JOptionPane.WARNING_MESSAGE);
                    pfPassword.requestFocus();
                    return;
                }
            }

            // ── Parse ngày vào làm ────────────────────────────────────────────
            LocalDateTime hireDate;
            String hireDateStr = tfHireDate.getText().trim();
            if (!hireDateStr.isEmpty()) {
                try {
                    hireDate = LocalDateTime.parse(hireDateStr, DATE_FMT);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Ngày vào làm không đúng định dạng (dd/MM/yyyy HH:mm).",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    tfHireDate.requestFocus();
                    return;
                }
            } else {
                hireDate = LocalDateTime.now();
            }

            Role role = cbRole.getSelectedIndex() == 1 ? Role.MANAGER : Role.PHARMACIST;

            // password gửi lên server: khi tạo mới thì gửi plain text,
            // server (StaffRepositoryImplementation.create) sẽ hash trước khi lưu.
            // Khi sửa thì để null — server giữ nguyên password cũ.
            StaffDto dto = StaffDto.builder()
                    .id(editTarget != null ? editTarget.getId() : null)
                    .username(tfUsername.getText().trim())
                    .fullName(tfFullName.getText().trim())
                    .licenseNumber(tfLicense.getText().trim())
                    .phoneNumber(phoneVal)
                    .email(emailVal)
                    .password(editTarget == null ? passwordVal : null)
                    .role(role)
                    .hireDate(hireDate)
                    .active(chkActive.isSelected())
                    .build();

            CommandType cmd = editTarget == null
                    ? CommandType.STAFF_CREATE : CommandType.STAFF_UPDATE;

            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            new SwingWorker<Response, Void>() {
                @Override protected Response doInBackground() throws Exception {
                    return networkService.send(cmd, dto);
                }

                @Override protected void done() {
                    try {
                        Response resp = get();
                        if (resp != null && resp.isSuccess()
                                && resp.getData() instanceof StaffDto saved) {
                            SwingUtilities.invokeLater(() -> {
                                onSaved.accept(saved);
                                dispose();
                            });
                        } else {
                            String msg = resp != null ? resp.getMessage()
                                    : "Không nhận được phản hồi từ server.";
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(StaffFormDialog.this,
                                        "Lưu thất bại: " + msg,
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                                resetSaveButton();
                            });
                        }
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        String msg = cause != null ? cause.getMessage() : e.getMessage();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(StaffFormDialog.this,
                                    "Lỗi kết nối: " + msg,
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            resetSaveButton();
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(StaffFormDialog.this,
                                    "Lỗi kết nối: " + e.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            resetSaveButton();
                        });
                    }
                }
            }.execute();
        }

        private void resetSaveButton() {
            btnSave.setEnabled(true);
            btnSave.setText(editTarget == null ? "Lưu nhân viên" : "Cập nhật");
        }

        // ── Helpers ───────────────────────────────────────────────────────────
        private JTextField field() {
            JTextField tf = new JTextField();
            tf.setFont(FD_INPUT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(D_BORDER, 1, true),
                    new EmptyBorder(6, 8, 6, 8)
            ));
            return tf;
        }

        private JLabel formLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(FD_LABEL);
            lbl.setForeground(D_ACCENT);
            return lbl;
        }

        private JLabel secLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(FD_SEC);
            lbl.setForeground(D_PRIMARY);
            lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
            return lbl;
        }

        private JButton dlgBtn(String text, Color bg) {
            JButton btn = new JButton(text);
            btn.setFont(FD_BTN);
            btn.setBackground(bg);
            btn.setForeground(AppColors.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
            return btn;
        }

        /** 2 cột: label | field | label | field */
        private void addFormRow(JPanel panel, GridBagConstraints gc, int row,
                                String lbl1, JComponent comp1,
                                String lbl2, JComponent comp2) {
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            panel.add(formLabel(lbl1), gc);

            gc.gridx = 1; gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(comp1, gc);

            gc.gridx = 2; gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            panel.add(formLabel(lbl2), gc);

            gc.gridx = 3; gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(comp2, gc);
        }

        /** 1 cột rộng: label | field (span 3 cột) */
        private void addFormRowSingle(JPanel panel, GridBagConstraints gc,
                                      int row, String lbl, JComponent comp) {
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            panel.add(formLabel(lbl), gc);

            gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 3;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(comp, gc);
            gc.gridwidth = 1;
        }

        private String nvl(String s) { return s != null ? s : ""; }
    }
}