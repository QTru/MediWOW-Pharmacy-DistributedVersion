package presentation;

import core.dto.LotDto;
import core.dto.MeasurementDto;
import core.dto.ProductDto;
import core.dto.UnitOfMeasureDto;
import core.entities.enums.DosageForm;
import core.entities.enums.LotStatus;
import core.entities.enums.ProductCategory;
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
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ProductGui extends JPanel {

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
    private static final String[] PRODUCT_COLS = {
            "Mã SP", "Mã vạch", "Tên sản phẩm", "Tên ngắn", "Danh mục", "Dạng bào chế", "Nhà SX"
    };
    private static final String[] UOM_COLS = {
            "Đơn vị", "Đơn vị gốc?", "Giá bán", "Tỷ lệ quy đổi"
    };
    private static final String[] LOT_COLS = {
            "Mã lô", "Số lô", "SL tồn", "Giá nhập", "HSD", "Trạng thái"
    };

    // ── Toolbar components ────────────────────────────────────────────────────
    private JTextField tfSearch;
    private JButton    btnSearch;
    private JButton    btnReload;
    private JButton    btnAdd;
    private JButton    btnEdit;

    // ── Product table ─────────────────────────────────────────────────────────
    private JTable            tblProduct;
    private DefaultTableModel mdlProduct;
    private TableRowSorter<DefaultTableModel> sorterProduct;

    // ── Detail panel ──────────────────────────────────────────────────────────
    private JTable            tblUom;
    private DefaultTableModel mdlUom;
    private JTable            tblLot;
    private DefaultTableModel mdlLot;
    private JPanel            pnlDetail;

    // ── State ─────────────────────────────────────────────────────────────────
    private final NetworkService networkService;
    private List<ProductDto>     productList = new ArrayList<>();
    private ProductDto           selectedProduct;

    // [GUARD] Cờ ngăn thao tác khi đang loading
    private boolean isLoading = false;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════════════════
    // Constructor
    // ═════════════════════════════════════════════════════════════════════════
    public ProductGui(NetworkService networkService) {
        this.networkService = networkService;
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);
        initComponents();
        loadProducts();
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

        JLabel lblTitle = new JLabel("Quản lý Sản phẩm");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(CLR_ACCENT);
        bar.add(lblTitle, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(CLR_WHITE);

        tfSearch = new JTextField(20);
        tfSearch.setFont(FONT_SEARCH);
        tfSearch.setToolTipText("Tìm theo tên, mã vạch, mã SP...");
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
        btnReload.addActionListener(e -> {
            // [GUARD] Không cho tải lại khi đang loading
            if (isLoading) return;
            loadProducts();
        });
        toolbar.add(btnReload);

        btnAdd = makeBtn("+ Thêm", CLR_SUCCESS);
        btnAdd.addActionListener(e -> {
            // [GUARD] Không mở dialog khi đang loading
            if (isLoading) {
                showWarn("Vui lòng chờ dữ liệu tải xong trước khi thêm sản phẩm.");
                return;
            }
            openAddDialog();
        });
        toolbar.add(btnAdd);

        // [GUARD] btnEdit disable sẵn, chỉ enable khi có dòng được chọn
        btnEdit = makeBtn("✎ Sửa", CLR_WARNING);
        btnEdit.setForeground(Color.BLACK);
        btnEdit.setEnabled(false);
        btnEdit.setToolTipText("Chọn một sản phẩm trong danh sách để sửa");
        btnEdit.addActionListener(e -> {
            // [GUARD] Không mở dialog khi đang loading
            if (isLoading) {
                showWarn("Vui lòng chờ dữ liệu tải xong trước khi sửa sản phẩm.");
                return;
            }
            openEditDialog();
        });
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

        mdlProduct = new DefaultTableModel(PRODUCT_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProduct = new JTable(mdlProduct);
        styleTable(tblProduct);

        sorterProduct = new TableRowSorter<>(mdlProduct);
        tblProduct.setRowSorter(sorterProduct);

        // [GUARD] Enable/disable btnEdit theo selection; chặn khi đang loading
        tblProduct.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = tblProduct.getSelectedRow() >= 0;
                // Chỉ enable btnEdit khi có selection VÀ không đang loading
                btnEdit.setEnabled(hasSelection && !isLoading);
                if (hasSelection) onProductSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(tblProduct);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JLabel lblCount = new JLabel("0 sản phẩm");
        lblCount.setFont(FONT_VALUE);
        lblCount.setForeground(CLR_MUTED);
        lblCount.setBorder(new EmptyBorder(6, 4, 0, 0));
        panel.add(lblCount, BorderLayout.SOUTH);

        mdlProduct.addTableModelListener(e2 ->
                lblCount.setText(mdlProduct.getRowCount() + " sản phẩm"));

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
        JLabel lbl = new JLabel("← Chọn một sản phẩm để xem chi tiết");
        lbl.setFont(FONT_VALUE);
        lbl.setForeground(CLR_MUTED);
        ph.add(lbl);
        return ph;
    }

    private JPanel buildDetailContent(ProductDto p) {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(CLR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 4;
        card.add(sectionLabel("Thông tin sản phẩm"), gc);
        gc.gridwidth = 1;
        gc.gridy++;

        int[] row = {gc.gridy};
        addDetailRow(card, gc, row, "Mã SP:",         p.getId(),          0);
        addDetailRow(card, gc, row, "Mã vạch:",       p.getBarcode(),     0);
        addDetailRow(card, gc, row, "Tên SP:",         p.getName(),        0);
        addDetailRow(card, gc, row, "Tên ngắn:",      p.getShortName(),   0);
        addDetailRow(card, gc, row, "Danh mục:",      categoryLabel(p.getCategory()), 0);
        addDetailRow(card, gc, row, "Dạng bào chế:", formLabel(p.getForm()), 0);
        addDetailRow(card, gc, row, "Nhà SX:",        p.getManufacturer(), 0);
        addDetailRow(card, gc, row, "Hoạt chất:",     p.getIngredients(),  0);
        addDetailRow(card, gc, row, "Hàm lượng:",
                p.getStrength() != null ? p.getStrength() : "—", 0);
        addDetailRow(card, gc, row, "VAT (%):",
                p.getVat() != null ? p.getVat().toPlainString() + "%" : "—", 0);
        addDetailRow(card, gc, row, "Mô tả:",
                p.getDescription() != null ? p.getDescription() : "—", 0);
        addDetailRow(card, gc, row, "Ngày tạo:",
                p.getCreationDate() != null ? p.getCreationDate().format(DATE_FMT) : "—", 0);

        root.add(card, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_LABEL);
        tabs.addTab("Đơn vị tính", buildUomPanel(p));
        tabs.addTab("Lô hàng",     buildLotPanel(p));
        root.add(tabs, BorderLayout.CENTER);

        return root;
    }

    private void addDetailRow(JPanel card, GridBagConstraints gc, int[] rowRef,
                              String label, String value, int colOffset) {
        gc.gridx = colOffset; gc.gridy = rowRef[0];
        gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_MUTED);
        card.add(lbl, gc);

        gc.gridx = colOffset + 1; gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(FONT_VALUE);
        val.setForeground(CLR_ACCENT);
        card.add(val, gc);
        rowRef[0]++;
    }

    private JPanel buildUomPanel(ProductDto p) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CLR_WHITE);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));

        mdlUom = new DefaultTableModel(UOM_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUom = new JTable(mdlUom);
        styleTable(tblUom);

        if (p.getUnitOfMeasures() != null) {
            for (UnitOfMeasureDto u : p.getUnitOfMeasures()) {
                String unitName = u.getMeasurement() != null ? u.getMeasurement().getName() : "—";
                String isBase   = u.isBaseUnit() ? "✔ Gốc" : "";
                String price    = u.getPrice() != null ? formatMoney(u.getPrice()) + " đ" : "—";
                String rate     = u.getBaseUnitConversionRate() != null
                        ? u.getBaseUnitConversionRate().toPlainString() : "—";
                mdlUom.addRow(new Object[]{ unitName, isBase, price, rate });
            }
        }

        JScrollPane scroll = new JScrollPane(tblUom);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        scroll.setPreferredSize(new Dimension(0, 130));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLotPanel(ProductDto p) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CLR_WHITE);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));

        mdlLot = new DefaultTableModel(LOT_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLot = new JTable(mdlLot);
        styleTable(tblLot);

        sendAsync(CommandType.LOT_LOAD_ALL, null,
                resp -> SwingUtilities.invokeLater(() -> {
                    if (resp != null && resp.getData() instanceof List<?> list) {
                        for (Object obj : list) {
                            if (obj instanceof LotDto lot
                                    && lot.getProductId().equals(p.getId())) {
                                String hsd = lot.getExpiryDate() != null
                                        ? lot.getExpiryDate().format(DATE_FMT) : "—";
                                mdlLot.addRow(new Object[]{
                                        lot.getId(),
                                        lot.getBatchNumber(),
                                        lot.getQuantity(),
                                        lot.getRawPrice() != null
                                                ? formatMoney(lot.getRawPrice()) + " đ" : "—",
                                        hsd,
                                        lotStatusLabel(lot.getStatus())
                                });
                            }
                        }
                    }
                }),
                ex -> {}
        );

        JScrollPane scroll = new JScrollPane(tblLot);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        scroll.setPreferredSize(new Dimension(0, 130));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
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
    private void loadProducts() {
        setLoading(true);
        sendAsync(CommandType.PRODUCT_LOAD_ALL, null,
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

                    productList.clear();
                    mdlProduct.setRowCount(0);
                    for (Object obj : list) {
                        if (obj instanceof ProductDto dto) {
                            productList.add(dto);
                            mdlProduct.addRow(new Object[]{
                                    dto.getId(), dto.getBarcode(), dto.getName(),
                                    dto.getShortName(), categoryLabel(dto.getCategory()),
                                    formLabel(dto.getForm()), dto.getManufacturer()
                            });
                        }
                    }

                    if (productList.isEmpty() && !list.isEmpty()) {
                        showError("Dữ liệu nhận được nhưng không parse được ProductDto.");
                    }
                }),
                ex -> SwingUtilities.invokeLater(() -> {
                    setLoading(false);
                    showError("Lỗi kết nối: " + ex.getMessage());
                })
        );
    }

    private void applySearch() {
        String keyword = tfSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            sorterProduct.setRowFilter(null);
        } else {
            sorterProduct.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Selection handler
    // ═════════════════════════════════════════════════════════════════════════
    private void onProductSelected() {
        int viewRow = tblProduct.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = tblProduct.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= productList.size()) return;

        selectedProduct = productList.get(modelRow);
        refreshDetailPanel(selectedProduct);
    }

    private void refreshDetailPanel(ProductDto p) {
        pnlDetail.removeAll();
        JScrollPane scroll = new JScrollPane(buildDetailContent(p));
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
        ProductFormDialog dialog = new ProductFormDialog(
                getParentFrame(), networkService, null, productList,
                saved -> {
                    productList.add(saved);
                    mdlProduct.addRow(new Object[]{
                            saved.getId(), saved.getBarcode(), saved.getName(),
                            saved.getShortName(), categoryLabel(saved.getCategory()),
                            formLabel(saved.getForm()), saved.getManufacturer()
                    });
                }
        );
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedProduct == null) {
            showWarn("Vui lòng chọn sản phẩm cần sửa.");
            return;
        }
        ProductFormDialog dialog = new ProductFormDialog(
                getParentFrame(), networkService, selectedProduct, productList,
                updated -> {
                    int row = productList.indexOf(selectedProduct);
                    if (row >= 0) {
                        productList.set(row, updated);
                        mdlProduct.setValueAt(updated.getId(),                      row, 0);
                        mdlProduct.setValueAt(updated.getBarcode(),                 row, 1);
                        mdlProduct.setValueAt(updated.getName(),                    row, 2);
                        mdlProduct.setValueAt(updated.getShortName(),               row, 3);
                        mdlProduct.setValueAt(categoryLabel(updated.getCategory()), row, 4);
                        mdlProduct.setValueAt(formLabel(updated.getForm()),         row, 5);
                        mdlProduct.setValueAt(updated.getManufacturer(),            row, 6);
                    }
                    selectedProduct = updated;
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
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(orig.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(orig);
            }
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

    private String categoryLabel(ProductCategory cat) {
        if (cat == null) return "—";
        return switch (cat) {
            case SUPPLEMENT -> "Thực phẩm chức năng";
            case OTC        -> "Thuốc không kê đơn";
            case ETC        -> "Thuốc kê đơn";
        };
    }

    private String formLabel(DosageForm form) {
        if (form == null) return "—";
        return switch (form) {
            case LIQUID_DOSAGE -> "Dạng lỏng";
            case SOLID_DOSAGE  -> "Dạng rắn";
        };
    }

    private String lotStatusLabel(LotStatus status) {
        if (status == null) return "—";
        return switch (status) {
            case AVAILABLE -> "Còn hàng";
            case EXPIRED   -> "Hết hạn";
            case FAULTY    -> "Lỗi / hỏng";
        };
    }

    private String formatMoney(BigDecimal val) {
        if (val == null) return "0";
        return String.format("%,.0f", val);
    }

    /**
     * [GUARD] Bật/tắt trạng thái loading:
     * - Disable toàn bộ toolbar controls
     * - Cập nhật cờ isLoading để các guard khác kiểm tra
     * - btnEdit chỉ enable khi KHÔNG loading VÀ có selection
     */
    private void setLoading(boolean loading) {
        if (btnReload == null) return;
        isLoading = loading;

        btnReload.setEnabled(!loading);
        btnReload.setText(loading ? "Đang tải..." : "Tải lại");

        btnAdd.setEnabled(!loading);
        btnSearch.setEnabled(!loading);
        tfSearch.setEnabled(!loading);

        // btnEdit chỉ enable khi không loading VÀ có dòng đang chọn
        boolean hasSelection = tblProduct != null && tblProduct.getSelectedRow() >= 0;
        btnEdit.setEnabled(!loading && hasSelection);
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
    // Inner class: ProductFormDialog
    // ═════════════════════════════════════════════════════════════════════════
    static class ProductFormDialog extends JDialog {

        private static final Color D_ACCENT  = AppColors.DARK;
        private static final Color D_BG      = AppColors.BACKGROUND;
        private static final Color D_WHITE   = AppColors.WHITE;
        private static final Color D_BORDER  = AppColors.LIGHT;
        private static final Color D_PRIMARY = AppColors.PRIMARY;
        private static final Color D_SUCCESS = AppColors.SUCCESS;
        private static final Color D_DANGER  = AppColors.DANGER;
        private static final Color D_WARNING = AppColors.WARNING;

        private static final Font FD_LABEL = new Font("Segoe UI", Font.BOLD,  14);
        private static final Font FD_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font FD_BTN   = new Font("Segoe UI", Font.BOLD,  14);
        private static final Font FD_TITLE = new Font("Segoe UI", Font.BOLD,  18);
        private static final Font FD_SEC   = new Font("Segoe UI", Font.BOLD,  15);

        private static final DateTimeFormatter HSD_FMT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ── Tab 1 fields ──────────────────────────────────────────────────────
        private JTextField        tfBarcode;
        private JTextField        tfName;
        private JTextField        tfShortName;
        private JComboBox<String> cbCategory;
        private JComboBox<String> cbForm;
        private JTextField        tfManufacturer;
        private JTextField        tfIngredients;
        private JTextField        tfStrength;
        private JTextField        tfVat;
        private JTextArea         taDescription;

        // ── Tab 2: Đơn vị tính ────────────────────────────────────────────────
        private static final String[] UOM_COLS_FORM = {
                "Đơn vị", "Đơn vị gốc?", "Giá bán (đ)", "Tỷ lệ quy đổi"
        };
        private DefaultTableModel mdlUom;
        private JTable            tblUom;
        private List<UomRowData>  uomRows = new ArrayList<>();

        // ── Tab 3: Lô hàng ────────────────────────────────────────────────────
        private static final String[] LOT_COLS_FORM = {
                "Số lô", "SL nhập", "Giá nhập (đ)", "HSD (dd/MM/yyyy)", "Trạng thái"
        };
        private DefaultTableModel mdlLot;
        private JTable            tblLot;
        private List<LotRowData>  lotRows = new ArrayList<>();

        // ── State ─────────────────────────────────────────────────────────────
        private final NetworkService       networkService;
        private final ProductDto           editTarget;
        private final Consumer<ProductDto> onSaved;
        private final List<ProductDto>     allProducts;   // [VALIDATE] để kiểm tra trùng mã vạch
        private List<MeasurementDto>       measurements = new ArrayList<>();

        // [GUARD] Cờ ngăn thao tác khi đang lưu
        private boolean isSaving = false;

        private JButton btnSave;

        // ── Constructor ───────────────────────────────────────────────────────
        /**
         * @param allProducts danh sách toàn bộ sản phẩm hiện có,
         *                    dùng để validate trùng mã vạch phía client.
         */
        ProductFormDialog(JFrame parent, NetworkService ns,
                          ProductDto editTarget,
                          List<ProductDto> allProducts,
                          Consumer<ProductDto> onSaved) {
            super(parent,
                    editTarget == null ? "Thêm sản phẩm mới" : "Sửa sản phẩm",
                    true);
            this.networkService = ns;
            this.editTarget     = editTarget;
            this.allProducts    = allProducts != null ? allProducts : List.of();
            this.onSaved        = onSaved;
            initDialog();
            loadMeasurements();
            if (editTarget != null) fillForm(editTarget);
        }

        // ── UI ────────────────────────────────────────────────────────────────
        private void initDialog() {
            setSize(820, 660);
            setMinimumSize(new Dimension(720, 580));
            setLocationRelativeTo(getOwner());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // [GUARD] Chặn đóng dialog khi đang lưu
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (isSaving) {
                        JOptionPane.showMessageDialog(ProductFormDialog.this,
                                "Đang lưu dữ liệu, vui lòng chờ...",
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // Chỉ đóng khi không đang lưu (setDefaultCloseOperation = DO_NOTHING
                    // nếu muốn chặn cứng; ở đây chỉ cảnh báo)
                }
            });

            getContentPane().setBackground(D_BG);
            getContentPane().setLayout(new BorderLayout(0, 0));

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(D_ACCENT);
            header.setBorder(new EmptyBorder(14, 20, 14, 20));
            JLabel lblTitle = new JLabel(
                    editTarget == null ? "Thêm sản phẩm mới" : "Sửa sản phẩm");
            lblTitle.setFont(FD_TITLE);
            lblTitle.setForeground(AppColors.WHITE);
            header.add(lblTitle, BorderLayout.WEST);
            getContentPane().add(header, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(FD_SEC);
            tabs.addTab("Thông tin cơ bản", buildBasicTab());
            tabs.addTab("Đơn vị tính",      buildUomTab());
            tabs.addTab("Lô hàng",          buildLotTab());
            tabs.setBorder(new EmptyBorder(10, 10, 0, 10));
            getContentPane().add(tabs, BorderLayout.CENTER);

            getContentPane().add(buildFooterButtons(), BorderLayout.SOUTH);
        }

        // ── Tab 1 ─────────────────────────────────────────────────────────────
        private JScrollPane buildBasicTab() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(D_WHITE);
            panel.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets  = new Insets(6, 6, 6, 6);
            gc.anchor  = GridBagConstraints.WEST;
            gc.fill    = GridBagConstraints.HORIZONTAL;

            int row = 0;

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(secLabel("Thông tin cơ bản"), gc);
            gc.gridwidth = 1; row++;

            tfBarcode      = field();
            tfName         = field();
            addFormRow(panel, gc, row++, "Mã vạch *", tfBarcode,
                    "Tên sản phẩm *", tfName);

            tfShortName    = field();
            tfManufacturer = field();
            addFormRow(panel, gc, row++, "Tên ngắn *", tfShortName,
                    "Nhà sản xuất *", tfManufacturer);

            cbCategory = new JComboBox<>(new String[]{
                    "Thực phẩm chức năng", "Thuốc không kê đơn", "Thuốc kê đơn"
            });
            cbForm = new JComboBox<>(new String[]{ "Dạng lỏng", "Dạng rắn" });
            styleCombo(cbCategory); styleCombo(cbForm);
            addFormRow(panel, gc, row++, "Danh mục *", cbCategory,
                    "Dạng bào chế *", cbForm);

            tfIngredients = field();
            tfStrength    = field();
            addFormRow(panel, gc, row++, "Hoạt chất *", tfIngredients,
                    "Hàm lượng", tfStrength);

            tfVat = field();
            tfVat.setText("0");
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
            panel.add(formLabel("VAT (%) *"), gc);
            gc.gridx = 1; gc.weightx = 1;
            panel.add(tfVat, gc);
            row++;

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(new JSeparator(), gc);
            gc.gridwidth = 1; row++;

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            panel.add(secLabel("Mô tả"), gc);
            gc.gridwidth = 1; row++;

            taDescription = new JTextArea(4, 30);
            taDescription.setFont(FD_INPUT);
            taDescription.setLineWrap(true);
            taDescription.setWrapStyleWord(true);
            taDescription.setBorder(new EmptyBorder(6, 8, 6, 8));
            JScrollPane descScroll = new JScrollPane(taDescription);
            descScroll.setBorder(BorderFactory.createLineBorder(D_BORDER, 1));
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
            gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
            panel.add(descScroll, gc);

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(12);
            return scroll;
        }

        // ── Tab 2: Đơn vị tính ────────────────────────────────────────────────
        private JPanel buildUomTab() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(D_WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            mdlUom = new DefaultTableModel(UOM_COLS_FORM, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tblUom = new JTable(mdlUom);
            styleTableForm(tblUom);

            JScrollPane scroll = new JScrollPane(tblUom);
            scroll.setBorder(BorderFactory.createLineBorder(D_BORDER));
            panel.add(scroll, BorderLayout.CENTER);

            JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            btnBar.setBackground(D_WHITE);

            JButton btnAddUom = dlgBtn("+ Thêm đơn vị", D_PRIMARY);
            JButton btnRemUom = dlgBtn("✕ Xóa",         D_DANGER);

            btnAddUom.addActionListener(e -> openAddUomDialog());

            // [CONFIRM] Xác nhận trước khi xóa UOM
            btnRemUom.addActionListener(e -> {
                int selRow = tblUom.getSelectedRow();
                if (selRow < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn đơn vị tính cần xóa.",
                            "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String unitName = uomRows.get(selRow).measurement() != null
                        ? uomRows.get(selRow).measurement().getName() : "?";
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Xóa đơn vị \"" + unitName + "\" khỏi danh sách?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    uomRows.remove(selRow);
                    mdlUom.removeRow(selRow);
                }
            });

            btnBar.add(btnAddUom);
            btnBar.add(btnRemUom);
            panel.add(btnBar, BorderLayout.SOUTH);
            return panel;
        }

        // ── Tab 3: Lô hàng ────────────────────────────────────────────────────
        private JPanel buildLotTab() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(D_WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            mdlLot = new DefaultTableModel(LOT_COLS_FORM, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tblLot = new JTable(mdlLot);
            styleTableForm(tblLot);

            JScrollPane scroll = new JScrollPane(tblLot);
            scroll.setBorder(BorderFactory.createLineBorder(D_BORDER));
            panel.add(scroll, BorderLayout.CENTER);

            JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            btnBar.setBackground(D_WHITE);

            JButton btnAddLot = dlgBtn("+ Thêm lô", D_PRIMARY);
            JButton btnRemLot = dlgBtn("✕ Xóa",     D_DANGER);

            btnAddLot.addActionListener(e -> openAddLotDialog());

            // [CONFIRM] Xác nhận trước khi xóa lô
            btnRemLot.addActionListener(e -> {
                int selRow = tblLot.getSelectedRow();
                if (selRow < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn lô hàng cần xóa.",
                            "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int batchNum = lotRows.get(selRow).batchNumber();
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Xóa lô số \"" + batchNum + "\" khỏi danh sách?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    lotRows.remove(selRow);
                    mdlLot.removeRow(selRow);
                }
            });

            btnBar.add(btnAddLot);
            btnBar.add(btnRemLot);
            panel.add(btnBar, BorderLayout.SOUTH);
            return panel;
        }

        // ── Dialog thêm lô ────────────────────────────────────────────────────
        private void openAddLotDialog() {
            JDialog dlg = new JDialog(this, "Thêm lô hàng", true);
            dlg.setSize(420, 340);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());
            dlg.getContentPane().setBackground(D_BG);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(D_WHITE);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets  = new Insets(6, 6, 6, 6);
            gc.anchor  = GridBagConstraints.WEST;
            gc.fill    = GridBagConstraints.HORIZONTAL;

            JTextField tfBatch  = field();
            tfBatch.setToolTipText("Nhập số nguyên, ví dụ: 2024001");
            JTextField tfQty    = field();
            JTextField tfPrice  = field();
            JTextField tfHsd    = field();
            tfHsd.setToolTipText("Định dạng: dd/MM/yyyy");

            JComboBox<String> cbStatus = new JComboBox<>(new String[]{
                    "Còn hàng", "Hết hạn", "Lỗi / hỏng"
            });
            styleCombo(cbStatus);

            int r = 0;
            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Số lô *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfBatch, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Số lượng nhập *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfQty, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Giá nhập (đ) *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfPrice, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("HSD (dd/MM/yyyy) *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfHsd, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Trạng thái"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(cbStatus, gc);

            dlg.add(form, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            foot.setBackground(D_BG);
            JButton btnOk     = dlgBtn("Thêm", D_SUCCESS);
            JButton btnCancel = dlgBtn("Hủy",  D_DANGER);
            btnCancel.addActionListener(e -> dlg.dispose());

            btnOk.addActionListener(e -> {
                String batch    = tfBatch.getText().trim();
                String qtyStr   = tfQty.getText().trim();
                String priceStr = tfPrice.getText().trim();
                String hsdStr   = tfHsd.getText().trim();

                // ── Kiểm tra bắt buộc ────────────────────────────────────────
                if (batch.isEmpty() || qtyStr.isEmpty()
                        || priceStr.isEmpty() || hsdStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg,
                            "Vui lòng điền đầy đủ các trường bắt buộc (*).",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int batchNumber;
                int qty;
                BigDecimal price;
                LocalDateTime hsd;

                // ── Validate số lô ────────────────────────────────────────────
                try {
                    batchNumber = Integer.parseInt(batch);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số lô phải là số nguyên.",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // [VALIDATE] Không cho trùng batchNumber trong cùng lần nhập
                boolean dupBatch = lotRows.stream()
                        .anyMatch(lr -> lr.batchNumber() == batchNumber);
                if (dupBatch) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số lô " + batchNumber + " đã tồn tại trong danh sách.\nVui lòng nhập số lô khác.",
                            "Trùng số lô", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ── Validate số lượng ─────────────────────────────────────────
                try {
                    qty = Integer.parseInt(qtyStr);
                    if (qty <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Số lượng phải là số nguyên dương (> 0).",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ── Validate giá nhập ─────────────────────────────────────────
                try {
                    price = new BigDecimal(priceStr.replace(",", ""));
                    if (price.compareTo(BigDecimal.ZERO) < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Giá nhập phải là số hợp lệ (≥ 0).",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ── Validate HSD ──────────────────────────────────────────────
                try {
                    hsd = LocalDate.parse(hsdStr, HSD_FMT).atStartOfDay();
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Hạn sử dụng không đúng định dạng dd/MM/yyyy.",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // [VALIDATE] Cảnh báo nếu HSD đã qua (không chặn cứng, cho phép nhập lô hỏng/hết hạn)
                if (hsd.isBefore(LocalDateTime.now())) {
                    int ok = JOptionPane.showConfirmDialog(dlg,
                            "Hạn sử dụng đã qua ngày hôm nay.\nBạn vẫn muốn thêm lô này?",
                            "Cảnh báo HSD", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (ok != JOptionPane.YES_OPTION) return;
                }

                LotStatus status = switch (cbStatus.getSelectedIndex()) {
                    case 1  -> LotStatus.EXPIRED;
                    case 2  -> LotStatus.FAULTY;
                    default -> LotStatus.AVAILABLE;
                };

                LotRowData lotRow = new LotRowData(batchNumber, qty, price, hsd, status);
                lotRows.add(lotRow);
                mdlLot.addRow(new Object[]{
                        batchNumber,
                        qty,
                        String.format("%,.0f", price) + " đ",
                        hsdStr,
                        cbStatus.getSelectedItem()
                });
                dlg.dispose();
            });

            foot.add(btnOk);
            foot.add(btnCancel);
            dlg.add(foot, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }

        // ── Dialog thêm đơn vị tính ───────────────────────────────────────────
        private void openAddUomDialog() {
            if (measurements.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Chưa tải được danh sách đơn vị đo lường.\nVui lòng thử lại sau.",
                        "Chưa có dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Thêm đơn vị tính", true);
            dlg.setSize(400, 320);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());
            dlg.getContentPane().setBackground(D_BG);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(D_WHITE);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill   = GridBagConstraints.HORIZONTAL;

            String[] measNames = measurements.stream()
                    .map(MeasurementDto::getName).toArray(String[]::new);
            JComboBox<String> cbMeas = new JComboBox<>(measNames);
            styleCombo(cbMeas);

            JCheckBox chkBase = new JCheckBox("Là đơn vị gốc");
            chkBase.setBackground(D_WHITE);
            chkBase.setFont(FD_INPUT);

            JTextField tfPrice = field();
            JTextField tfRate  = field();
            tfRate.setText("1");

            chkBase.addActionListener(e -> {
                boolean base = chkBase.isSelected();
                tfRate.setEnabled(!base);
                if (base) tfRate.setText("1");
            });

            int r = 0;
            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Đơn vị *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(cbMeas, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Giá bán (đ) *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfPrice, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            form.add(formLabel("Tỷ lệ quy đổi *"), gc);
            gc.gridx = 1; gc.weightx = 1; form.add(tfRate, gc); r++;

            gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
            form.add(chkBase, gc);

            dlg.add(form, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            foot.setBackground(D_BG);
            JButton btnOk     = dlgBtn("Thêm", D_SUCCESS);
            JButton btnCancel = dlgBtn("Hủy",  D_DANGER);
            btnCancel.addActionListener(e -> dlg.dispose());

            btnOk.addActionListener(e -> {
                String mName = (String) cbMeas.getSelectedItem();
                MeasurementDto meas = measurements.stream()
                        .filter(m -> m.getName().equals(mName))
                        .findFirst().orElse(null);
                if (meas == null) return;

                String priceStr = tfPrice.getText().trim();
                String rateStr  = tfRate.getText().trim();

                if (priceStr.isEmpty() || rateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg,
                            "Vui lòng nhập đầy đủ thông tin.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                BigDecimal price;
                BigDecimal rate;
                try {
                    price = new BigDecimal(priceStr.replace(",", ""));
                    if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Giá bán phải là số hợp lệ (≥ 0).",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    rate = new BigDecimal(rateStr);
                    if (rate.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Tỷ lệ quy đổi phải là số dương (> 0).",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean isBase = chkBase.isSelected();

                // [VALIDATE] Không cho trùng đơn vị đo lường trong cùng sản phẩm
                boolean dupMeas = uomRows.stream()
                        .anyMatch(row -> row.measurement() != null
                                && row.measurement().getId().equals(meas.getId()));
                if (dupMeas) {
                    JOptionPane.showMessageDialog(dlg,
                            "Đơn vị \"" + meas.getName() + "\" đã tồn tại trong danh sách.\nVui lòng chọn đơn vị khác.",
                            "Trùng đơn vị", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // [VALIDATE] Nếu chọn là đơn vị gốc nhưng đã có rồi → cảnh báo
                if (isBase) {
                    boolean alreadyHasBase = uomRows.stream().anyMatch(UomRowData::baseUnit);
                    if (alreadyHasBase) {
                        JOptionPane.showMessageDialog(dlg,
                                "Đã có một đơn vị gốc trong danh sách.\nMỗi sản phẩm chỉ được có một đơn vị gốc duy nhất.",
                                "Trùng đơn vị gốc", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                uomRows.add(new UomRowData(meas, isBase, price, rate));
                mdlUom.addRow(new Object[]{
                        meas.getName(),
                        isBase ? "✔ Gốc" : "",
                        String.format("%,.0f", price) + " đ",
                        rate.toPlainString()
                });
                dlg.dispose();
            });

            foot.add(btnOk);
            foot.add(btnCancel);
            dlg.add(foot, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }

        // ── Footer buttons ─────────────────────────────────────────────────────
        private JPanel buildFooterButtons() {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(D_BG);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, D_BORDER));

            JButton btnCancel = dlgBtn("Hủy", D_DANGER);
            btnSave = dlgBtn(editTarget == null ? "Lưu sản phẩm" : "Cập nhật", D_SUCCESS);

            // [GUARD] Nút Hủy cũng kiểm tra trạng thái lưu
            btnCancel.addActionListener(e -> {
                if (isSaving) {
                    JOptionPane.showMessageDialog(this,
                            "Đang lưu dữ liệu, không thể đóng lúc này.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                dispose();
            });

            btnSave.addActionListener(e -> saveProduct());

            footer.add(btnCancel);
            footer.add(btnSave);
            return footer;
        }

        // ── Fill form ─────────────────────────────────────────────────────────
        private void fillForm(ProductDto p) {
            tfBarcode.setText(nvl(p.getBarcode()));
            tfName.setText(nvl(p.getName()));
            tfShortName.setText(nvl(p.getShortName()));
            tfManufacturer.setText(nvl(p.getManufacturer()));
            tfIngredients.setText(nvl(p.getIngredients()));
            tfStrength.setText(nvl(p.getStrength()));
            tfVat.setText(p.getVat() != null ? p.getVat().toPlainString() : "0");
            taDescription.setText(nvl(p.getDescription()));

            if (p.getCategory() != null)
                cbCategory.setSelectedIndex(switch (p.getCategory()) {
                    case SUPPLEMENT -> 0; case OTC -> 1; case ETC -> 2;
                });
            if (p.getForm() != null)
                cbForm.setSelectedIndex(switch (p.getForm()) {
                    case LIQUID_DOSAGE -> 0; case SOLID_DOSAGE -> 1;
                });

            if (p.getUnitOfMeasures() != null) {
                for (UnitOfMeasureDto u : p.getUnitOfMeasures()) {
                    uomRows.add(new UomRowData(
                            u.getMeasurement(), u.isBaseUnit(),
                            u.getPrice(), u.getBaseUnitConversionRate()));
                    String unitName = u.getMeasurement() != null
                            ? u.getMeasurement().getName() : "—";
                    mdlUom.addRow(new Object[]{
                            unitName,
                            u.isBaseUnit() ? "✔ Gốc" : "",
                            u.getPrice() != null
                                    ? String.format("%,.0f", u.getPrice()) + " đ" : "—",
                            u.getBaseUnitConversionRate() != null
                                    ? u.getBaseUnitConversionRate().toPlainString() : "—"
                    });
                }
            }
        }

        // ── Save ──────────────────────────────────────────────────────────────
        private void saveProduct() {
            // [GUARD] Không cho lưu nhiều lần cùng lúc
            if (isSaving) return;

            // ── Validate Tab 1: Thông tin cơ bản ─────────────────────────────
            if (tfBarcode.getText().isBlank() || tfName.getText().isBlank()
                    || tfShortName.getText().isBlank()
                    || tfManufacturer.getText().isBlank()
                    || tfIngredients.getText().isBlank()
                    || tfVat.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đầy đủ các trường bắt buộc (*) ở tab \"Thông tin cơ bản\".",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal vat;
            try {
                vat = new BigDecimal(tfVat.getText().trim());
                if (vat.compareTo(BigDecimal.ZERO) < 0
                        || vat.compareTo(new BigDecimal("100")) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "VAT phải nằm trong khoảng 0 – 100 (%).",
                            "Lỗi giá trị", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "VAT phải là số hợp lệ (ví dụ: 10).",
                        "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // [VALIDATE] Kiểm tra trùng mã vạch với các sản phẩm khác
            String barcode = tfBarcode.getText().trim();
            boolean dupBarcode = allProducts.stream()
                    .filter(p -> editTarget == null || !p.getId().equals(editTarget.getId()))
                    .anyMatch(p -> barcode.equalsIgnoreCase(p.getBarcode()));
            if (dupBarcode) {
                JOptionPane.showMessageDialog(this,
                        "Mã vạch \"" + barcode + "\" đã được sử dụng bởi sản phẩm khác.\nVui lòng nhập mã vạch khác.",
                        "Trùng mã vạch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Validate Tab 2: Đơn vị tính ──────────────────────────────────
            if (uomRows.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Sản phẩm phải có ít nhất một đơn vị tính.\nVui lòng thêm đơn vị tính ở tab \"Đơn vị tính\".",
                        "Thiếu đơn vị tính", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long baseCount = uomRows.stream().filter(UomRowData::baseUnit).count();
            if (baseCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "Phải có đúng một đơn vị tính là đơn vị gốc (✔ Gốc).\nVui lòng kiểm tra tab \"Đơn vị tính\".",
                        "Thiếu đơn vị gốc", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // baseCount > 1 không xảy ra vì đã chặn từ openAddUomDialog,
            // nhưng giữ lại để phòng trường hợp fillForm() load dữ liệu lỗi từ server
            if (baseCount > 1) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ được có đúng một đơn vị gốc. Hiện có " + baseCount + " đơn vị được đánh dấu là gốc.\nVui lòng kiểm tra lại.",
                        "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // [CONFIRM] Xác nhận trước khi gửi lên server
            String action = editTarget == null ? "thêm sản phẩm mới" : "cập nhật sản phẩm này";
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn " + action + "?\n\n"
                            + "Tên: " + tfName.getText().trim() + "\n"
                            + "Mã vạch: " + barcode + "\n"
                            + "Số đơn vị tính: " + uomRows.size() + "\n"
                            + "Số lô hàng sẽ thêm: " + lotRows.size(),
                    "Xác nhận lưu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            // ── Build DTO ─────────────────────────────────────────────────────
            ProductCategory cat = switch (cbCategory.getSelectedIndex()) {
                case 1  -> ProductCategory.OTC;
                case 2  -> ProductCategory.ETC;
                default -> ProductCategory.SUPPLEMENT;
            };
            DosageForm form = cbForm.getSelectedIndex() == 0
                    ? DosageForm.LIQUID_DOSAGE : DosageForm.SOLID_DOSAGE;

            List<UnitOfMeasureDto> uoms = uomRows.stream()
                    .map(rd -> UnitOfMeasureDto.builder()
                            .measurement(rd.measurement())
                            .baseUnit(rd.baseUnit())
                            .price(rd.price())
                            .baseUnitConversionRate(rd.rate())
                            .build())
                    .toList();

            ProductDto dto = ProductDto.builder()
                    .id(editTarget != null ? editTarget.getId() : null)
                    .barcode(barcode)
                    .name(tfName.getText().trim())
                    .shortName(tfShortName.getText().trim())
                    .manufacturer(tfManufacturer.getText().trim())
                    .ingredients(tfIngredients.getText().trim())
                    .strength(tfStrength.getText().isBlank()
                            ? null : tfStrength.getText().trim())
                    .vat(vat)
                    .description(taDescription.getText().isBlank()
                            ? null : taDescription.getText().trim())
                    .category(cat)
                    .form(form)
                    .unitOfMeasures(uoms)
                    .creationDate(editTarget != null
                            ? editTarget.getCreationDate() : LocalDateTime.now())
                    .build();

            CommandType cmd = editTarget == null
                    ? CommandType.PRODUCT_CREATE : CommandType.PRODUCT_UPDATE;

            // [GUARD] Đánh dấu đang lưu, disable nút
            setSaving(true);

            new SwingWorker<Response, Void>() {
                @Override protected Response doInBackground() throws Exception {
                    return networkService.send(cmd, dto);
                }

                @Override protected void done() {
                    try {
                        Response resp = get();
                        if (resp != null && resp.getData() instanceof ProductDto saved) {
                            saveLots(saved);
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                String msg = resp != null ? resp.getMessage() : "Không có phản hồi từ server.";
                                JOptionPane.showMessageDialog(ProductFormDialog.this,
                                        "Lưu thất bại: " + msg,
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                                setSaving(false);
                            });
                        }
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ProductFormDialog.this,
                                    "Lỗi kết nối: " + ex.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            setSaving(false);
                        });
                    }
                }
            }.execute();
        }

        /** Gửi các lô hàng tuần tự sau khi product đã được lưu thành công. */
        private void saveLots(ProductDto savedProduct) {
            if (lotRows.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    onSaved.accept(savedProduct);
                    dispose();
                });
                return;
            }

            SwingUtilities.invokeLater(() ->
                    btnSave.setText("Đang lưu lô... (0/" + lotRows.size() + ")"));

            new SwingWorker<List<String>, Void>() {
                @Override
                protected List<String> doInBackground() {
                    List<String> errors = new ArrayList<>();
                    int idx = 0;
                    for (LotRowData lr : lotRows) {
                        idx++;
                        final int current = idx;
                        SwingUtilities.invokeLater(() ->
                                btnSave.setText("Đang lưu lô... ("
                                        + current + "/" + lotRows.size() + ")"));

                        LotDto lotDto = LotDto.builder()
                                .productId(savedProduct.getId())
                                .batchNumber(lr.batchNumber())
                                .quantity(lr.quantity())
                                .rawPrice(lr.rawPrice())
                                .expiryDate(lr.expiryDate())
                                .status(lr.status())
                                .build();
                        try {
                            Response r = networkService.send(CommandType.LOT_CREATE, lotDto);
                            if (r == null || !r.isSuccess()) {
                                errors.add("Lô #" + lr.batchNumber() + ": "
                                        + (r != null ? r.getMessage() : "Không phản hồi"));
                            }
                        } catch (Exception ex) {
                            errors.add("Lô #" + lr.batchNumber() + ": " + ex.getMessage());
                        }
                    }
                    return errors;
                }

                @Override
                protected void done() {
                    try {
                        List<String> errors = get();
                        SwingUtilities.invokeLater(() -> {
                            if (!errors.isEmpty()) {
                                JOptionPane.showMessageDialog(ProductFormDialog.this,
                                        "Sản phẩm đã lưu nhưng một số lô bị lỗi:\n"
                                                + String.join("\n", errors),
                                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                            }
                            onSaved.accept(savedProduct);
                            dispose();
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ProductFormDialog.this,
                                    "Lỗi khi lưu lô hàng: " + ex.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            setSaving(false);
                        });
                    }
                }
            }.execute();
        }

        // ── Load measurements ─────────────────────────────────────────────────
        private void loadMeasurements() {
            new SwingWorker<Response, Void>() {
                @Override protected Response doInBackground() throws Exception {
                    return networkService.send(CommandType.MEASUREMENT_LOAD_ALL, null);
                }
                @Override protected void done() {
                    try {
                        Response resp = get();
                        if (resp != null && resp.getData() instanceof List<?> list) {
                            measurements.clear();
                            for (Object obj : list)
                                if (obj instanceof MeasurementDto m)
                                    measurements.add(m);
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        }

        // ── Guard helpers ─────────────────────────────────────────────────────
        /**
         * [GUARD] Bật/tắt trạng thái đang lưu:
         * - Disable toàn bộ nút khi đang gửi lên server
         * - Cập nhật text btnSave
         */
        private void setSaving(boolean saving) {
            isSaving = saving;
            btnSave.setEnabled(!saving);
            if (!saving) {
                btnSave.setText(editTarget == null ? "Lưu sản phẩm" : "Cập nhật");
            } else {
                btnSave.setText("Đang lưu...");
            }
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
            lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
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

        private void styleCombo(JComboBox<?> cb) {
            cb.setFont(FD_INPUT);
            cb.setBackground(D_WHITE);
        }

        private void styleTableForm(JTable table) {
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setRowHeight(30);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setSelectionBackground(new Color(0xB3E0F0));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            table.getTableHeader().setBackground(D_ACCENT);
            table.getTableHeader().setForeground(AppColors.WHITE);
            table.getTableHeader().setReorderingAllowed(false);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                                                               boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    setBorder(new EmptyBorder(4, 10, 4, 10));
                    setBackground(sel ? new Color(0xB3E0F0)
                            : (row % 2 == 0 ? D_WHITE : new Color(0xEEF7FA)));
                    setForeground(D_ACCENT);
                    return this;
                }
            });
        }

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

        private String nvl(String s) { return s != null ? s : ""; }

        // ── Records ───────────────────────────────────────────────────────────
        record UomRowData(MeasurementDto measurement, boolean baseUnit,
                          BigDecimal price, BigDecimal rate) {}

        record LotRowData(int batchNumber, int quantity,
                          BigDecimal rawPrice, LocalDateTime expiryDate,
                          LotStatus status) {}
    }
}