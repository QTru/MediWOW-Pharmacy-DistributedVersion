package presentation;

import core.dto.*;
import core.entities.enums.*;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PromotionGui extends JPanel {
    private static final Color CLR_BG = AppColors.BACKGROUND;
    private static final Color CLR_WHITE = AppColors.WHITE;
    private static final Color CLR_ACCENT = AppColors.DARK;
    private static final Color CLR_PRIMARY = AppColors.PRIMARY;
    private static final Color CLR_MUTED = AppColors.PLACEHOLDER_TEXT;
    private static final Color CLR_BORDER = AppColors.LIGHT;
    private static final Color CLR_SUCCESS = AppColors.SUCCESS;
    private static final Color CLR_DANGER = AppColors.DANGER;
    private static final Color CLR_WARNING = AppColors.WARNING;
    private static final Color CLR_ROW_EVEN = new Color(0xEEF7FA);
    private static final Color CLR_ROW_SEL = new Color(0xB3E0F0);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 14);

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NetworkService networkService;

    private JTextField tfSearch;
    private JButton btnReload;
    private JButton btnAdd;
    private JButton btnToggleActive;

    private JTable tblPromotion;
    private DefaultTableModel mdlPromotion;
    private TableRowSorter<DefaultTableModel> sorterPromotion;

    private JPanel pnlDetail;

    private List<PromotionDto> promotionList = new ArrayList<>();
    private PromotionDto selectedPromotion;

    public PromotionGui(NetworkService networkService) {
        this.networkService = networkService;
        setLayout(new BorderLayout());
        setBackground(CLR_BG);
        initComponents();
        loadPromotions();
    }

    private void initComponents() {
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(CLR_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, CLR_BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("Quản lý Khuyến mại");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_ACCENT);
        bar.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(CLR_WHITE);

        tfSearch = new JTextField(20);
        tfSearch.setFont(FONT_VALUE);
        tfSearch.setToolTipText("Tìm theo mã hoặc tên khuyến mại");
        tfSearch.addActionListener(e -> applySearch());
        toolbar.add(tfSearch);

        JButton btnSearch = makeBtn("Tìm", CLR_PRIMARY);
        btnSearch.addActionListener(e -> applySearch());
        toolbar.add(btnSearch);

        btnReload = makeBtn("Tải lại", CLR_ACCENT);
        btnReload.addActionListener(e -> loadPromotions());
        toolbar.add(btnReload);

        btnAdd = makeBtn("+ Thêm khuyến mại", CLR_SUCCESS);
        btnAdd.addActionListener(e -> openAddDialog());
        toolbar.add(btnAdd);

        btnToggleActive = makeBtn("Bật/Tắt", CLR_WARNING);
        btnToggleActive.setForeground(Color.BLACK);
        btnToggleActive.addActionListener(e -> toggleActive());
        toolbar.add(btnToggleActive);

        bar.add(toolbar, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildMainArea() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(),
                buildDetailPanel()
        );
        split.setDividerLocation(720);
        split.setDividerSize(5);
        split.setContinuousLayout(true);
        split.setBorder(null);
        return split;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 6));

        String[] cols = {
                "Mã KM", "Tên khuyến mại", "Hiệu lực từ", "Kết thúc",
                "Trạng thái", "Active", "Số ĐK", "Số HĐ"
        };

        mdlPromotion = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblPromotion = new JTable(mdlPromotion);
        styleTable(tblPromotion);

        sorterPromotion = new TableRowSorter<>(mdlPromotion);
        tblPromotion.setRowSorter(sorterPromotion);

        tblPromotion.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onPromotionSelected();
        });

        JScrollPane scroll = new JScrollPane(tblPromotion);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        panel.add(scroll, BorderLayout.CENTER);

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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        JLabel label = new JLabel("← Chọn một khuyến mại để xem chi tiết");
        label.setFont(FONT_VALUE);
        label.setForeground(CLR_MUTED);
        panel.add(label);
        return panel;
    }

    private void loadPromotions() {
        setLoading(true);

        sendAsync(
                CommandType.PROMOTION_LOAD_ALL,
                null,
                response -> {
                    setLoading(false);

                    if (response == null || !response.isSuccess()) {
                        showError(response == null ? "Server không phản hồi." : response.getMessage());
                        return;
                    }

                    Object data = response.getData();
                    if (!(data instanceof List<?> list)) {
                        showError("Dữ liệu khuyến mại trả về không hợp lệ.");
                        return;
                    }

                    promotionList.clear();
                    mdlPromotion.setRowCount(0);

                    for (Object obj : list) {
                        if (obj instanceof PromotionDto dto) {
                            promotionList.add(dto);
                            addPromotionRow(dto);
                        }
                    }

                    pnlDetail.removeAll();
                    pnlDetail.add(buildPlaceholderDetail(), BorderLayout.CENTER);
                    pnlDetail.revalidate();
                    pnlDetail.repaint();
                },
                error -> {
                    setLoading(false);
                    showError("Lỗi tải khuyến mại: " + error.getMessage());
                }
        );
    }

    private void addPromotionRow(PromotionDto dto) {
        mdlPromotion.addRow(new Object[]{
                dto.getId(),
                dto.getName(),
                formatDateTime(dto.getEffectiveDate()),
                formatDateTime(dto.getEndDate()),
                statusText(dto),
                dto.isActive() ? "Có" : "Không",
                dto.getConditions() == null ? 0 : dto.getConditions().size(),
                dto.getActions() == null ? 0 : dto.getActions().size()
        });
    }

    private void applySearch() {
        String keyword = tfSearch.getText().trim();
        sorterPromotion.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + keyword));
    }

    private void onPromotionSelected() {
        int viewRow = tblPromotion.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tblPromotion.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= promotionList.size()) return;

        selectedPromotion = promotionList.get(modelRow);
        refreshDetail(selectedPromotion);
    }

    private void refreshDetail(PromotionDto p) {
        pnlDetail.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(CLR_BG);

        root.add(buildBasicInfoCard(p), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_LABEL);
        tabs.addTab("Điều kiện", buildConditionTable(p));
        tabs.addTab("Hành động", buildActionTable(p));
        root.add(tabs, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        pnlDetail.add(scroll, BorderLayout.CENTER);
        pnlDetail.revalidate();
        pnlDetail.repaint();
    }

    private JPanel buildBasicInfoCard(PromotionDto p) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int row = 0;
        addDetailRow(card, gc, row++, "Mã KM:", p.getId());
        addDetailRow(card, gc, row++, "Tên:", p.getName());
        addDetailRow(card, gc, row++, "Mô tả:", p.getDescription());
        addDetailRow(card, gc, row++, "Ngày tạo:", formatDateTime(p.getCreationDate()));
        addDetailRow(card, gc, row++, "Hiệu lực từ:", formatDateTime(p.getEffectiveDate()));
        addDetailRow(card, gc, row++, "Kết thúc:", formatDateTime(p.getEndDate()));
        addDetailRow(card, gc, row++, "Trạng thái:", statusText(p));
        addDetailRow(card, gc, row, "Active:", p.isActive() ? "Có" : "Không");

        return card;
    }

    private void addDetailRow(JPanel card, GridBagConstraints gc, int row, String label, String value) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_MUTED);
        card.add(lbl, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        JLabel val = new JLabel(value == null || value.isBlank() ? "—" : value);
        val.setFont(FONT_VALUE);
        val.setForeground(CLR_ACCENT);
        card.add(val, gc);
    }

    private JScrollPane buildConditionTable(PromotionDto p) {
        String[] cols = {"Loại", "Sản phẩm", "Đơn vị", "So sánh", "Giá trị"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        if (p.getConditions() != null) {
            for (PromotionConditionDto c : p.getConditions()) {
                model.addRow(new Object[]{
                        conditionTypeText(c.getType()),
                        c.getProductName() != null ? c.getProductName() : "—",
                        c.getMeasurementName() != null ? c.getMeasurementName() : "—",
                        "≥",
                        c.getValue()
                });
            }
        }

        JTable table = new JTable(model);
        styleTable(table);
        return new JScrollPane(table);
    }

    private JScrollPane buildActionTable(PromotionDto p) {
        String[] cols = {"Thứ tự", "Loại", "Mục tiêu", "Sản phẩm", "Đơn vị", "Giá trị"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        if (p.getActions() != null) {
            for (PromotionActionDto a : p.getActions()) {
                model.addRow(new Object[]{
                        a.getActionOrder(),
                        actionTypeText(a.getType()),
                        targetText(a.getTarget()),
                        a.getProductName() != null ? a.getProductName() : "—",
                        a.getMeasurementName() != null ? a.getMeasurementName() : "—",
                        a.getValue()
                });
            }
        }

        JTable table = new JTable(model);
        styleTable(table);
        return new JScrollPane(table);
    }

    private void openAddDialog() {
        PromotionFormDialog dialog = new PromotionFormDialog(
                getParentFrame(),
                networkService,
                saved -> loadPromotions()
        );
        dialog.setVisible(true);
    }

    private void toggleActive() {
        if (selectedPromotion == null) {
            showWarn("Vui lòng chọn khuyến mại cần bật/tắt.");
            return;
        }

        boolean newActive = !selectedPromotion.isActive();

        PromotionActiveRequestDto request = PromotionActiveRequestDto.builder()
                .promotionId(selectedPromotion.getId())
                .active(newActive)
                .build();

        sendAsync(
                CommandType.PROMOTION_SET_ACTIVE,
                request,
                response -> {
                    if (response == null || !response.isSuccess()) {
                        showError(response == null ? "Server không phản hồi." : response.getMessage());
                        return;
                    }
                    loadPromotions();
                },
                error -> showError("Lỗi bật/tắt khuyến mại: " + error.getMessage())
        );
    }

    private String statusText(PromotionDto p) {
        LocalDateTime now = LocalDateTime.now();

        if (!p.isActive()) return "Đã tắt";
        if (p.getEffectiveDate() != null && now.isBefore(p.getEffectiveDate())) return "Sắp diễn ra";
        if (p.getEndDate() != null && now.isAfter(p.getEndDate())) return "Hết hạn";
        return "Đang áp dụng";
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(DATE_TIME_FMT);
    }

    private String conditionTypeText(ConditionType type) {
        if (type == null) return "—";
        return switch (type) {
            case PRODUCT_QTY -> "Số lượng sản phẩm";
            case ORDER_SUBTOTAL -> "Tổng tiền đơn hàng";
            case PRODUCT_ID -> "Mã sản phẩm";
        };
    }

    private String actionTypeText(ActionType type) {
        if (type == null) return "—";
        return switch (type) {
            case PERCENT_DISCOUNT -> "Giảm giá %";
            case FIXED_DISCOUNT -> "Giảm tiền cố định";
            case PRODUCT_GIFT -> "Tặng sản phẩm";
        };
    }

    private String targetText(Target target) {
        if (target == null) return "—";
        return switch (target) {
            case PRODUCT -> "Sản phẩm";
            case ORDER_SUBTOTAL -> "Tổng đơn hàng";
        };
    }

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
            final Color original = bg;
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(original.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });

        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_VALUE);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(CLR_ROW_SEL);
        table.setSelectionForeground(CLR_ACCENT);
        table.setFocusable(false);

        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setBackground(CLR_ACCENT);
        table.getTableHeader().setForeground(AppColors.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col
            ) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(new EmptyBorder(4, 10, 4, 10));
                if (sel) {
                    setBackground(CLR_ROW_SEL);
                    setForeground(CLR_ACCENT);
                } else {
                    setBackground(row % 2 == 0 ? CLR_WHITE : CLR_ROW_EVEN);
                    setForeground(CLR_ACCENT);
                }
                return this;
            }
        });
    }

    private void setLoading(boolean loading) {
        btnReload.setEnabled(!loading);
        btnReload.setText(loading ? "Đang tải..." : "Tải lại");
    }

    private JFrame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return w instanceof JFrame f ? f : null;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void sendAsync(
            CommandType command,
            Object data,
            Consumer<Response> onSuccess,
            Consumer<Exception> onError
    ) {
        // Network calls run inside SwingWorker so the Swing UI thread stays responsive.
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception {
                return networkService.send(command, data);
            }

            @Override protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    onError.accept(cause instanceof Exception ex ? ex : new RuntimeException(cause));
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    static class PromotionFormDialog extends JDialog {
        private static final DateTimeFormatter DATE_TIME_FMT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        private final NetworkService networkService;
        private final Consumer<PromotionDto> onSaved;

        private JTextField tfName;
        private JTextField tfEffectiveDate;
        private JTextField tfEndDate;
        private JTextArea taDescription;

        private DefaultTableModel mdlConditions;
        private JTable tblConditions;
        private List<PromotionConditionDto> conditions = new ArrayList<>();

        private DefaultTableModel mdlActions;
        private JTable tblActions;
        private List<PromotionActionDto> actions = new ArrayList<>();

        private List<ProductDto> products = new ArrayList<>();

        private JButton btnSave;

        PromotionFormDialog(JFrame parent, NetworkService networkService, Consumer<PromotionDto> onSaved) {
            super(parent, "Thêm khuyến mại", true);
            this.networkService = networkService;
            this.onSaved = onSaved;

            initDialog();
            loadProducts();
        }

        private void initDialog() {
            setSize(860, 660);
            setMinimumSize(new Dimension(760, 580));
            setLocationRelativeTo(getOwner());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().setBackground(AppColors.BACKGROUND);

            JLabel header = new JLabel("Thêm khuyến mại");
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            header.setForeground(AppColors.WHITE);
            header.setOpaque(true);
            header.setBackground(AppColors.DARK);
            header.setBorder(new EmptyBorder(14, 20, 14, 20));
            getContentPane().add(header, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(FONT_LABEL);
            tabs.addTab("Thông tin chung", buildBasicTab());
            tabs.addTab("Điều kiện", buildConditionTab());
            tabs.addTab("Hành động", buildActionTab());
            tabs.setBorder(new EmptyBorder(10, 10, 0, 10));
            getContentPane().add(tabs, BorderLayout.CENTER);

            getContentPane().add(buildFooter(), BorderLayout.SOUTH);
        }

        private JPanel buildBasicTab() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(AppColors.WHITE);
            panel.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            tfName = field();
            tfEffectiveDate = field();
            tfEndDate = field();
            taDescription = new JTextArea(5, 30);
            taDescription.setFont(FONT_VALUE);
            taDescription.setLineWrap(true);
            taDescription.setWrapStyleWord(true);

            tfEffectiveDate.setToolTipText("Ví dụ: 15/05/2026 08:00");
            tfEndDate.setToolTipText("Ví dụ: 30/05/2026 23:59");

            int row = 0;
            addRow(panel, gc, row++, "Tên khuyến mại *", tfName);
            addRow(panel, gc, row++, "Ngày bắt đầu *", tfEffectiveDate);
            addRow(panel, gc, row++, "Ngày kết thúc *", tfEndDate);

            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            panel.add(label("Mô tả"), gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.BOTH;
            gc.weighty = 1;
            JScrollPane scroll = new JScrollPane(taDescription);
            panel.add(scroll, gc);

            return panel;
        }

        private JPanel buildConditionTab() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(AppColors.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            // All promotion conditions are AND conditions.
            // Invoice must satisfy every condition to use this promotion.
            String[] cols = {"Loại", "Sản phẩm", "Đơn vị", "Giá trị"};
            mdlConditions = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            tblConditions = new JTable(mdlConditions);
            styleSmallTable(tblConditions);
            panel.add(new JScrollPane(tblConditions), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.setBackground(AppColors.WHITE);

            JButton btnAddCondition = dlgBtn("+ Thêm điều kiện", AppColors.PRIMARY);
            JButton btnRemoveCondition = dlgBtn("Xóa", AppColors.DANGER);

            btnAddCondition.addActionListener(e -> openAddConditionDialog());
            btnRemoveCondition.addActionListener(e -> removeSelectedCondition());

            buttons.add(btnAddCondition);
            buttons.add(btnRemoveCondition);
            panel.add(buttons, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel buildActionTab() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(AppColors.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[] cols = {"Thứ tự", "Loại", "Mục tiêu", "Sản phẩm", "Đơn vị", "Giá trị"};
            mdlActions = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            tblActions = new JTable(mdlActions);
            styleSmallTable(tblActions);
            panel.add(new JScrollPane(tblActions), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.setBackground(AppColors.WHITE);

            JButton btnAddAction = dlgBtn("+ Thêm hành động", AppColors.PRIMARY);
            JButton btnRemoveAction = dlgBtn("Xóa", AppColors.DANGER);

            btnAddAction.addActionListener(e -> openAddActionDialog());
            btnRemoveAction.addActionListener(e -> removeSelectedAction());

            buttons.add(btnAddAction);
            buttons.add(btnRemoveAction);
            panel.add(buttons, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel buildFooter() {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(AppColors.BACKGROUND);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, AppColors.LIGHT));

            JButton btnCancel = dlgBtn("Hủy", AppColors.DANGER);
            btnSave = dlgBtn("Lưu khuyến mại", AppColors.SUCCESS);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> savePromotion());

            footer.add(btnCancel);
            footer.add(btnSave);

            return footer;
        }

        private void openAddConditionDialog() {
            JDialog dialog = new JDialog(this, "Thêm điều kiện", true);
            dialog.setSize(520, 360);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(AppColors.WHITE);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            JComboBox<String> cbType = new JComboBox<>(new String[]{
                    "Số lượng sản phẩm",
                    "Tổng tiền đơn hàng"
            });

            JComboBox<ProductDto> cbProduct = new JComboBox<>();
            JComboBox<UnitOfMeasureDto> cbUom = new JComboBox<>();
            JTextField tfValue = field();

            for (ProductDto p : products) cbProduct.addItem(p);

            cbProduct.setRenderer(productRenderer());
            cbUom.setRenderer(uomRenderer());

            Runnable updateUom = () -> {
                cbUom.removeAllItems();
                ProductDto selected = (ProductDto) cbProduct.getSelectedItem();
                if (selected != null && selected.getUnitOfMeasures() != null) {
                    for (UnitOfMeasureDto uom : selected.getUnitOfMeasures()) {
                        cbUom.addItem(uom);
                    }
                }
            };

            cbProduct.addActionListener(e -> updateUom.run());
            updateUom.run();

            cbType.addActionListener(e -> {
                boolean productQty = cbType.getSelectedIndex() == 0;
                cbProduct.setEnabled(productQty);
                cbUom.setEnabled(productQty);
            });

            int row = 0;
            addRow(form, gc, row++, "Loại điều kiện *", cbType);
            addRow(form, gc, row++, "Sản phẩm", cbProduct);
            addRow(form, gc, row++, "Đơn vị tính", cbUom);
            addRow(form, gc, row, "Giá trị *", tfValue);

            dialog.add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(AppColors.BACKGROUND);

            JButton btnOk = dlgBtn("Thêm", AppColors.SUCCESS);
            JButton btnCancel = dlgBtn("Hủy", AppColors.DANGER);
            btnCancel.addActionListener(e -> dialog.dispose());

            btnOk.addActionListener(e -> {
                try {
                    BigDecimal value = new BigDecimal(tfValue.getText().trim());
                    if (value.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException();
                    }

                    boolean productQty = cbType.getSelectedIndex() == 0;

                    PromotionConditionDto dto = PromotionConditionDto.builder()
                            .type(productQty ? ConditionType.PRODUCT_QTY : ConditionType.ORDER_SUBTOTAL)
                            .target(productQty ? Target.PRODUCT : Target.ORDER_SUBTOTAL)
                            .comparator(core.entities.enums.Comparator.GREATER_EQUAL)
                            .value(value)
                            .build();

                    if (productQty) {
                        ProductDto product = (ProductDto) cbProduct.getSelectedItem();
                        UnitOfMeasureDto uom = (UnitOfMeasureDto) cbUom.getSelectedItem();

                        if (product == null || uom == null || uom.getMeasurement() == null) {
                            JOptionPane.showMessageDialog(dialog, "Vui lòng chọn sản phẩm và đơn vị.");
                            return;
                        }

                        dto.setProductId(product.getId());
                        dto.setProductName(product.getName());
                        dto.setMeasurementId(uom.getMeasurement().getId());
                        dto.setMeasurementName(uom.getMeasurement().getName());
                    }

                    conditions.add(dto);
                    mdlConditions.addRow(new Object[]{
                            productQty ? "Số lượng sản phẩm" : "Tổng tiền đơn hàng",
                            dto.getProductName() == null ? "—" : dto.getProductName(),
                            dto.getMeasurementName() == null ? "—" : dto.getMeasurementName(),
                            dto.getValue()
                    });
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Giá trị phải là số lớn hơn 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            footer.add(btnOk);
            footer.add(btnCancel);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        private void openAddActionDialog() {
            JDialog dialog = new JDialog(this, "Thêm hành động", true);
            dialog.setSize(560, 430);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(AppColors.WHITE);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            JTextField tfOrder = field();

            JComboBox<String> cbType = new JComboBox<>(new String[]{
                    "Giảm giá %",
                    "Giảm tiền cố định",
                    "Tặng sản phẩm"
            });

            JComboBox<String> cbTarget = new JComboBox<>(new String[]{
                    "Sản phẩm",
                    "Tổng đơn hàng"
            });

            JComboBox<ProductDto> cbProduct = new JComboBox<>();
            JComboBox<UnitOfMeasureDto> cbUom = new JComboBox<>();
            JTextField tfValue = field();

            for (ProductDto p : products) cbProduct.addItem(p);

            cbProduct.setRenderer(productRenderer());
            cbUom.setRenderer(uomRenderer());

            Runnable updateUom = () -> {
                cbUom.removeAllItems();
                ProductDto selected = (ProductDto) cbProduct.getSelectedItem();
                if (selected != null && selected.getUnitOfMeasures() != null) {
                    for (UnitOfMeasureDto uom : selected.getUnitOfMeasures()) {
                        cbUom.addItem(uom);
                    }
                }
            };

            cbProduct.addActionListener(e -> updateUom.run());
            updateUom.run();

            Runnable updateVisibility = () -> {
                boolean gift = cbType.getSelectedIndex() == 2;
                boolean targetProduct = cbTarget.getSelectedIndex() == 0;

                cbTarget.setEnabled(!gift);
                if (gift) cbTarget.setSelectedIndex(0);

                cbProduct.setEnabled(gift || targetProduct);
                cbUom.setEnabled(gift || targetProduct);
            };

            cbType.addActionListener(e -> updateVisibility.run());
            cbTarget.addActionListener(e -> updateVisibility.run());
            updateVisibility.run();

            int row = 0;
            addRow(form, gc, row++, "Thứ tự áp dụng *", tfOrder);
            addRow(form, gc, row++, "Loại hành động *", cbType);
            addRow(form, gc, row++, "Áp dụng cho", cbTarget);
            addRow(form, gc, row++, "Sản phẩm", cbProduct);
            addRow(form, gc, row++, "Đơn vị tính", cbUom);
            addRow(form, gc, row, "Giá trị *", tfValue);

            dialog.add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(AppColors.BACKGROUND);

            JButton btnOk = dlgBtn("Thêm", AppColors.SUCCESS);
            JButton btnCancel = dlgBtn("Hủy", AppColors.DANGER);
            btnCancel.addActionListener(e -> dialog.dispose());

            btnOk.addActionListener(e -> {
                try {
                    int order = Integer.parseInt(tfOrder.getText().trim());
                    BigDecimal value = new BigDecimal(tfValue.getText().trim());

                    if (order <= 0 || value.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException();
                    }

                    ActionType actionType = switch (cbType.getSelectedIndex()) {
                        case 1 -> ActionType.FIXED_DISCOUNT;
                        case 2 -> ActionType.PRODUCT_GIFT;
                        default -> ActionType.PERCENT_DISCOUNT;
                    };

                    Target target = actionType == ActionType.PRODUCT_GIFT
                            ? Target.PRODUCT
                            : (cbTarget.getSelectedIndex() == 0 ? Target.PRODUCT : Target.ORDER_SUBTOTAL);

                    if (actionType == ActionType.PERCENT_DISCOUNT
                            && value.compareTo(BigDecimal.valueOf(100)) > 0) {
                        JOptionPane.showMessageDialog(dialog, "Phần trăm giảm không được lớn hơn 100.");
                        return;
                    }

                    PromotionActionDto dto = PromotionActionDto.builder()
                            .actionOrder(order)
                            .type(actionType)
                            .target(target)
                            .value(value)
                            .build();

                    if (target == Target.PRODUCT) {
                        ProductDto product = (ProductDto) cbProduct.getSelectedItem();
                        UnitOfMeasureDto uom = (UnitOfMeasureDto) cbUom.getSelectedItem();

                        if (product == null || uom == null || uom.getMeasurement() == null) {
                            JOptionPane.showMessageDialog(dialog, "Vui lòng chọn sản phẩm và đơn vị.");
                            return;
                        }

                        dto.setProductId(product.getId());
                        dto.setProductName(product.getName());
                        dto.setMeasurementId(uom.getMeasurement().getId());
                        dto.setMeasurementName(uom.getMeasurement().getName());
                    }

                    actions.add(dto);
                    mdlActions.addRow(new Object[]{
                            dto.getActionOrder(),
                            actionText(dto.getType()),
                            targetText(dto.getTarget()),
                            dto.getProductName() == null ? "—" : dto.getProductName(),
                            dto.getMeasurementName() == null ? "—" : dto.getMeasurementName(),
                            dto.getValue()
                    });
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Thứ tự và giá trị phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            footer.add(btnOk);
            footer.add(btnCancel);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        private void removeSelectedCondition() {
            int row = tblConditions.getSelectedRow();
            if (row < 0) return;

            int modelRow = tblConditions.convertRowIndexToModel(row);
            conditions.remove(modelRow);
            mdlConditions.removeRow(modelRow);
        }

        private void removeSelectedAction() {
            int row = tblActions.getSelectedRow();
            if (row < 0) return;

            int modelRow = tblActions.convertRowIndexToModel(row);
            actions.remove(modelRow);
            mdlActions.removeRow(modelRow);
        }

        private void savePromotion() {
            if (tfName.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(this, "Tên khuyến mại không được để trống.");
                return;
            }

            if (conditions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khuyến mại phải có ít nhất một điều kiện.");
                return;
            }

            if (actions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khuyến mại phải có ít nhất một hành động.");
                return;
            }

            LocalDateTime effective;
            LocalDateTime end;

            try {
                effective = LocalDateTime.parse(tfEffectiveDate.getText().trim(), DATE_TIME_FMT);
                end = LocalDateTime.parse(tfEndDate.getText().trim(), DATE_TIME_FMT);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày phải đúng định dạng dd/MM/yyyy HH:mm.");
                return;
            }

            if (!effective.isBefore(end)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc.");
                return;
            }

            PromotionDto dto = PromotionDto.builder()
                    .name(tfName.getText().trim())
                    .description(taDescription.getText().trim().isBlank() ? null : taDescription.getText().trim())
                    .effectiveDate(effective)
                    .endDate(end)
                    .active(true)
                    .conditions(conditions)
                    .actions(actions)
                    .build();

            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            sendAsync(
                    CommandType.PROMOTION_CREATE,
                    dto,
                    response -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu khuyến mại");

                        if (response == null || !response.isSuccess()) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    response == null ? "Server không phản hồi." : response.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }

                        if (response.getData() instanceof PromotionDto saved) {
                            onSaved.accept(saved);
                        }

                        dispose();
                    },
                    error -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu khuyến mại");
                        JOptionPane.showMessageDialog(this, error.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
            );
        }

        private void loadProducts() {
            sendAsync(
                    CommandType.PRODUCT_LOAD_ALL,
                    null,
                    response -> {
                        if (response != null && response.isSuccess() && response.getData() instanceof List<?> list) {
                            products.clear();
                            for (Object obj : list) {
                                if (obj instanceof ProductDto product) {
                                    products.add(product);
                                }
                            }
                        }
                    },
                    error -> JOptionPane.showMessageDialog(this, "Không tải được sản phẩm: " + error.getMessage())
            );
        }

        private JTextField field() {
            JTextField tf = new JTextField();
            tf.setFont(FONT_VALUE);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.LIGHT),
                    new EmptyBorder(6, 8, 6, 8)
            ));
            return tf;
        }

        private JLabel label(String text) {
            JLabel label = new JLabel(text);
            label.setFont(FONT_LABEL);
            label.setForeground(AppColors.DARK);
            return label;
        }

        private void addRow(JPanel panel, GridBagConstraints gc, int row, String labelText, JComponent component) {
            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            panel.add(label(labelText), gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(component, gc);
        }

        private JButton dlgBtn(String text, Color bg) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_BTN);
            btn.setBackground(bg);
            btn.setForeground(AppColors.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
            return btn;
        }

        private ListCellRenderer<? super ProductDto> productRenderer() {
            return new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
                ) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof ProductDto p) {
                        setText(p.getName() + " (" + p.getId() + ")");
                    }
                    return this;
                }
            };
        }

        private ListCellRenderer<? super UnitOfMeasureDto> uomRenderer() {
            return new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
                ) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof UnitOfMeasureDto uom && uom.getMeasurement() != null) {
                        String base = uom.isBaseUnit() ? " — base unit" : "";
                        String rate = uom.getBaseUnitConversionRate() == null
                                ? ""
                                : " — conversion rate: " + uom.getBaseUnitConversionRate();
                        setText(uom.getMeasurement().getName() + base + rate);
                    }
                    return this;
                }
            };
        }

        private static void styleSmallTable(JTable table) {
            table.setFont(FONT_VALUE);
            table.setRowHeight(30);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(FONT_LABEL);
            table.getTableHeader().setBackground(AppColors.DARK);
            table.getTableHeader().setForeground(AppColors.WHITE);
        }

        private String actionText(ActionType type) {
            return switch (type) {
                case PERCENT_DISCOUNT -> "Giảm giá %";
                case FIXED_DISCOUNT -> "Giảm tiền cố định";
                case PRODUCT_GIFT -> "Tặng sản phẩm";
            };
        }

        private String targetText(Target target) {
            return switch (target) {
                case PRODUCT -> "Sản phẩm";
                case ORDER_SUBTOTAL -> "Tổng đơn hàng";
            };
        }

        private void sendAsync(
                CommandType command,
                Object data,
                Consumer<Response> onSuccess,
                Consumer<Exception> onError
        ) {
            new SwingWorker<Response, Void>() {
                @Override protected Response doInBackground() throws Exception {
                    return networkService.send(command, data);
                }

                @Override protected void done() {
                    try {
                        onSuccess.accept(get());
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        onError.accept(cause instanceof Exception ex ? ex : new RuntimeException(cause));
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                }
            }.execute();
        }
    }
}