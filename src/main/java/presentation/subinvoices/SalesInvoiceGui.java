package presentation.subinvoices;

import core.dto.InvoiceDto;
import core.dto.InvoiceLineDto;
import core.dto.LotAllocationDto;
import core.dto.LotDto;
import core.dto.ProductDto;
import core.dto.PromotionActionDto;
import core.dto.PromotionConditionDto;
import core.dto.PromotionDto;
import core.dto.ShiftDto;
import core.dto.StaffDto;
import core.dto.UnitOfMeasureDto;
import core.entities.enums.ActionType;
import core.entities.enums.Comparator;
import core.entities.enums.ConditionType;
import core.entities.enums.InvoiceLineType;
import core.entities.enums.InvoiceType;
import core.entities.enums.LotStatus;
import core.entities.enums.PaymentMethod;
import core.entities.enums.ProductCategory;
import core.entities.enums.Target;
import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;
import presentation.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class SalesInvoiceGui extends JPanel implements
        ActionListener,
        DocumentListener,
        FocusListener,
        KeyListener,
        MouseListener,
        ListSelectionListener,
        TableModelListener,
        PropertyChangeListener {

    private static final int LEFT_MIN = 750;
    private static final int RIGHT_MIN = 530;
    private static final Pattern PRESCRIPTION_PATTERN = Pattern.compile("^[a-zA-Z0-9]{5}[a-zA-Z0-9]{7}-[NHCnhc]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final String[] TABLE_COLUMNS = {
            "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Đơn giá", "Thành tiền"
    };

    private final NetworkService networkService;
    private final StaffDto currentStaff;
    private final DecimalFormat currencyFormat;

    private final List<CatalogProduct> catalogProducts = new ArrayList<>();
    private final Map<String, CatalogProduct> catalogProductById = new LinkedHashMap<>();
    private final Map<String, List<LotDto>> lotsByProductId = new HashMap<>();
    private final List<PromotionDto> promotionCatalog = new ArrayList<>();
    private final List<InvoiceLineState> invoiceLines = new ArrayList<>();
    private final List<PromotionDto> applicablePromotions = new ArrayList<>();

    private ShiftDto currentShift;
    private PromotionDto selectedPromotion;

    private JTextField txtSearch;
    private JTable tblInvoiceLines;
    private DefaultTableModel invoiceTableModel;
    private JButton btnRemoveAll;
    private JButton btnRemoveSelected;

    private JTextField txtShiftId;
    private JTextField txtPrescriptionCode;
    private JTextField txtCustomerPhone;
    private JComboBox<Object> cmbPromotion;
    private DefaultComboBoxModel<Object> promotionModel;

    private JTextField txtVat;
    private JTextField txtDiscount;
    private JTextField txtTotal;
    private JFormattedTextField txtCustomerPayment;
    private JRadioButton rdoCash;
    private JRadioButton rdoBank;
    private JPanel pnlCashOptions;
    private JButton btnProcessPayment;

    private JWindow suggestionWindow;
    private JList<SearchSuggestion> suggestionList;
    private DefaultListModel<SearchSuggestion> suggestionModel;

    private boolean suppressTableEvents;
    private boolean suppressPromotionEvents;
    private int lastEditedRow = -1;

    public SalesInvoiceGui(NetworkService networkService, StaffDto currentStaff) {
        this.networkService = Objects.requireNonNull(networkService, "networkService");
        this.currentStaff = Objects.requireNonNull(currentStaff, "currentStaff");
        this.currencyFormat = createCurrencyFormat();

        setLayout(new BorderLayout(0, 0));
        setBackground(AppColors.BACKGROUND);
        buildUi();
        loadInitialData();
    }

    private void buildUi() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(AppColors.WHITE);
        leftPanel.setMinimumSize(new Dimension(LEFT_MIN, 0));
        leftPanel.setBorder(new EmptyBorder(12, 12, 12, 6));
        leftPanel.add(buildSearchPanel(), BorderLayout.NORTH);
        leftPanel.add(buildInvoiceTablePanel(), BorderLayout.CENTER);
        leftPanel.add(buildInvoiceTableActions(), BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppColors.WHITE);
        rightPanel.setMinimumSize(new Dimension(RIGHT_MIN, 0));
        rightPanel.setBorder(new EmptyBorder(12, 6, 12, 12));
        rightPanel.add(buildInvoiceInfoPanel(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerLocation(780);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppColors.WHITE);

        JLabel lblSearch = new JLabel("Tìm kiếm thuốc:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSearch.setForeground(AppColors.TEXT);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.LIGHT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtSearch.getDocument().addDocumentListener(this);
        txtSearch.addFocusListener(this);
        txtSearch.addKeyListener(this);

        panel.add(lblSearch, BorderLayout.WEST);
        panel.add(txtSearch, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInvoiceTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppColors.WHITE);

        JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(AppColors.DARK);
        panel.add(lblTitle, BorderLayout.NORTH);

        invoiceTableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row < 0 || row >= invoiceLines.size()) {
                    return false;
                }
                InvoiceLineState lineState = invoiceLines.get(row);
                return !lineState.gift && (column == 2 || column == 3);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 3 -> Integer.class;
                    case 4, 5 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };

        tblInvoiceLines = new JTable(invoiceTableModel);
        tblInvoiceLines.setRowHeight(32);
        tblInvoiceLines.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblInvoiceLines.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblInvoiceLines.setSelectionBackground(new Color(0xB3E0F0));
        tblInvoiceLines.setGridColor(AppColors.LIGHT);
        tblInvoiceLines.setShowVerticalLines(false);
        tblInvoiceLines.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblInvoiceLines.getTableHeader().setBackground(AppColors.DARK);
        tblInvoiceLines.getTableHeader().setForeground(AppColors.WHITE);
        tblInvoiceLines.getTableHeader().setReorderingAllowed(false);
        tblInvoiceLines.getSelectionModel().addListSelectionListener(this);
        tblInvoiceLines.getModel().addTableModelListener(this);
        tblInvoiceLines.addPropertyChangeListener(this);
        tblInvoiceLines.getColumnModel().getColumn(2).setCellEditor(new UnitOfMeasureCellEditor());
        tblInvoiceLines.getColumnModel().getColumn(3).setCellEditor(new QuantitySpinnerEditor());
        tblInvoiceLines.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
        tblInvoiceLines.getColumnModel().getColumn(5).setCellRenderer(new CurrencyRenderer());

        JScrollPane scrollPane = new JScrollPane(tblInvoiceLines);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.LIGHT, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInvoiceTableActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(AppColors.WHITE);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnRemoveAll = createActionButton("Xóa tất cả", "btnRemoveAll");
        btnRemoveSelected = createActionButton("Xóa sản phẩm", "btnRemoveSelected");
        panel.add(btnRemoveAll);
        panel.add(btnRemoveSelected);
        return panel;
    }

    private JPanel buildInvoiceInfoPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AppColors.WHITE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.LIGHT, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel lblTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(AppColors.DARK);
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppColors.WHITE);
        body.add(buildGeneralInfoPanel());
        body.add(Box.createVerticalStrut(14));
        body.add(buildPaymentInfoPanel());
        body.add(Box.createVerticalStrut(16));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        btnProcessPayment = createActionButton("Thanh toán", "btnProcessPayment");
        footer.add(btnProcessPayment);
        body.add(footer);

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildGeneralInfoPanel() {
        JPanel panel = createSectionPanel("Thông tin chung");

        txtShiftId = createReadOnlyField();
        txtPrescriptionCode = createInputField();
        txtPrescriptionCode.addFocusListener(this);
        txtCustomerPhone = createInputField();

        promotionModel = new DefaultComboBoxModel<>();
        cmbPromotion = new JComboBox<>(promotionModel);
        cmbPromotion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbPromotion.setRenderer(new PromotionRenderer());
        // IMPORTANT: add the initial element BEFORE attaching the ActionListener.
        // DefaultComboBoxModel.addElement() fires setSelectedItem() which fires ActionEvent.
        // If the listener were attached first, actionPerformed → refreshTableAndSummary()
        // would be called while txtVat / txtTotal / etc. are still null → NPE.
        promotionModel.addElement("Không có khuyến mãi");
        cmbPromotion.addActionListener(this); // attach listener AFTER initial population

        panel.add(createFormRow("Mã ca:", txtShiftId));
        panel.add(createFormRow("Mã đơn kê thuốc:", txtPrescriptionCode));
        panel.add(createFormRow("SĐT khách hàng:", txtCustomerPhone));
        panel.add(createFormRow("Khuyến mãi:", cmbPromotion));
        return panel;
    }

    private JPanel buildPaymentInfoPanel() {
        JPanel panel = createSectionPanel("Thông tin thanh toán");

        txtVat = createReadOnlyField();
        txtDiscount = createReadOnlyField();
        txtTotal = createReadOnlyField();

        NumberFormatter formatter = new NumberFormatter(createCurrencyFormat());
        formatter.setValueClass(Long.class);
        formatter.setMinimum(0L);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        txtCustomerPayment = new JFormattedTextField(formatter);
        txtCustomerPayment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCustomerPayment.setValue(0L);
        txtCustomerPayment.addFocusListener(this);
        txtCustomerPayment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.LIGHT, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));

        rdoCash = new JRadioButton("Tiền mặt", true);
        rdoBank = new JRadioButton("Ngân hàng/Ví điện tử");
        for (JRadioButton radioButton : List.of(rdoCash, rdoBank)) {
            radioButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            radioButton.setBackground(AppColors.WHITE);
            radioButton.addActionListener(this);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(rdoCash);
        group.add(rdoBank);

        JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        paymentMethodPanel.setOpaque(false);
        paymentMethodPanel.add(rdoCash);
        paymentMethodPanel.add(rdoBank);

        pnlCashOptions = new JPanel(new GridLayout(0, 3, 8, 8));
        pnlCashOptions.setOpaque(false);

        panel.add(createFormRow("VAT:", txtVat));
        panel.add(createFormRow("Tiền giảm giá:", txtDiscount));
        panel.add(createFormRow("Tổng tiền:", txtTotal));
        panel.add(createFormRow("Tiền khách đưa:", txtCustomerPayment));
        panel.add(createFormRow("Phương thức thanh toán:", paymentMethodPanel));
        panel.add(pnlCashOptions);

        refreshCashButtons();
        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppColors.LIGHT),
                new EmptyBorder(10, 0, 0, 0)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(AppColors.PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(165, 32));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(AppColors.TEXT);

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField createReadOnlyField() {
        JTextField textField = createInputField();
        textField.setEditable(false);
        textField.setFocusable(false);
        return textField;
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.LIGHT, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return textField;
    }

    private JButton createActionButton(String text, String name) {
        JButton button = new JButton(text);
        button.setName(name);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(AppColors.WHITE);
        button.setForeground(AppColors.PRIMARY);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.LIGHT, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        button.addActionListener(this);
        return button;
    }

    private void loadInitialData() {
        setSalesControlsEnabled(false);

        new SwingWorker<DataBundle, Void>() {
            @Override
            protected DataBundle doInBackground() throws Exception {
                List<ProductDto> products = extractList(networkService.send(CommandType.PRODUCT_LOAD_ALL), ProductDto.class);
                List<LotDto> lots = extractList(networkService.send(CommandType.LOT_LOAD_ALL), LotDto.class);
                List<PromotionDto> promotions = extractList(networkService.send(CommandType.PROMOTION_LOAD_ALL), PromotionDto.class);
                Response shiftResponse = networkService.send(CommandType.SHIFT_GET_ACTIVE, getCurrentWorkstation());
                ShiftDto shiftDto = shiftResponse.getData() instanceof ShiftDto dto ? dto : null;
                return new DataBundle(products, lots, promotions, shiftDto);
            }

            @Override
            protected void done() {
                try {
                    DataBundle dataBundle = get();
                    rebuildCatalog(dataBundle.products(), dataBundle.lots());
                    promotionCatalog.clear();
                    promotionCatalog.addAll(dataBundle.promotions());
                    currentShift = dataBundle.activeShift();
                    txtShiftId.setText(currentShift != null ? currentShift.getId() : "Chưa có ca làm việc");
                    refreshPromotionChoices();
                    refreshTableAndSummary();
                    setSalesControlsEnabled(true);
                    if (currentShift == null) {
                        JOptionPane.showMessageDialog(
                                SalesInvoiceGui.this,
                                "Chưa có ca làm việc đang mở trên máy này. Bạn vẫn có thể chuẩn bị hóa đơn, nhưng chưa thể thanh toán.",
                                "Chưa có ca làm việc",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception exception) {
                    setSalesControlsEnabled(true);
                    showError("Không thể tải dữ liệu bán hàng: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private <T> List<T> extractList(Response response, Class<T> itemType) {
        if (response == null || !response.isSuccess()) {
            throw new IllegalStateException(response == null ? "Không có phản hồi từ server." : response.getMessage());
        }
        List<T> results = new ArrayList<>();
        if (response.getData() instanceof List<?> list) {
            for (Object item : list) {
                if (itemType.isInstance(item)) {
                    results.add(itemType.cast(item));
                }
            }
        }
        return results;
    }

    private void rebuildCatalog(List<ProductDto> products, List<LotDto> lots) {
        catalogProducts.clear();
        catalogProductById.clear();
        lotsByProductId.clear();

        LocalDate today = LocalDate.now();
        for (LotDto lot : lots) {
            boolean available = lot.getStatus() == LotStatus.AVAILABLE
                    && lot.getQuantity() > 0
                    && lot.getExpiryDate() != null
                    && lot.getExpiryDate().toLocalDate().isAfter(today);
            if (available) {
                lotsByProductId.computeIfAbsent(lot.getProductId(), ignored -> new ArrayList<>()).add(lot);
            }
        }

        for (List<LotDto> lotList : lotsByProductId.values()) {
            lotList.sort(java.util.Comparator.comparing(LotDto::getExpiryDate));
        }

        for (ProductDto product : products) {
            List<LotDto> availableLots = lotsByProductId.getOrDefault(product.getId(), List.of());
            int totalQuantity = availableLots.stream().mapToInt(LotDto::getQuantity).sum();
            if (totalQuantity <= 0 || product.getUnitOfMeasures() == null || product.getUnitOfMeasures().isEmpty()) {
                continue;
            }

            CatalogProduct catalogProduct = new CatalogProduct(product, availableLots);
            if (catalogProduct.baseUnitOfMeasure == null) {
                continue;
            }
            catalogProducts.add(catalogProduct);
            catalogProductById.put(product.getId(), catalogProduct);
        }
    }

    private void setSalesControlsEnabled(boolean enabled) {
        txtSearch.setEnabled(enabled);
        tblInvoiceLines.setEnabled(enabled);
        btnRemoveAll.setEnabled(enabled);
        btnRemoveSelected.setEnabled(enabled);
        btnProcessPayment.setEnabled(enabled);
        cmbPromotion.setEnabled(enabled);
        rdoCash.setEnabled(enabled);
        rdoBank.setEnabled(enabled);
        txtCustomerPayment.setEnabled(enabled && rdoCash.isSelected());
    }

    private void updateSearchSuggestions() {
        String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        ensureSuggestionWindow();
        suggestionModel.clear();

        if (keyword.isBlank()) {
            suggestionWindow.setVisible(false);
            return;
        }

        for (CatalogProduct catalogProduct : catalogProducts) {
            ProductDto product = catalogProduct.product;
            if (containsIgnoreCase(product.getId(), keyword)
                    || containsIgnoreCase(product.getName(), keyword)
                    || containsIgnoreCase(product.getShortName(), keyword)) {
                suggestionModel.addElement(new SearchSuggestion(catalogProduct));
            }
        }

        if (suggestionModel.isEmpty()) {
            suggestionWindow.setVisible(false);
            return;
        }

        suggestionList.setSelectedIndex(0);

        // Position the window below the search field
        try {
            Point screenLoc = txtSearch.getLocationOnScreen();
            int rowHeight = suggestionList.getFixedCellHeight();
            if (rowHeight <= 0) rowHeight = 28;
            int windowHeight = Math.min(suggestionModel.size(), 7) * rowHeight + 4;
            suggestionWindow.setLocation(screenLoc.x, screenLoc.y + txtSearch.getHeight());
            suggestionWindow.setSize(txtSearch.getWidth(), windowHeight);
            suggestionWindow.setVisible(true);
        } catch (IllegalComponentStateException ignored) {
            // component not yet on screen
        }
    }

    /**
     * Lazily creates the non-focusable JWindow used for search suggestions.
     * Using JWindow instead of JPopupMenu is critical: a JWindow with
     * setFocusableWindowState(false) can never steal focus from txtSearch,
     * so focusLost never fires spuriously and the list stays visible.
     */
    private void ensureSuggestionWindow() {
        if (suggestionWindow != null) {
            return;
        }

        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        suggestionList.setFixedCellHeight(30);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setBackground(AppColors.WHITE);
        suggestionList.setSelectionBackground(new Color(0xB3E0F0));
        suggestionList.addMouseListener(this);

        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.LIGHT, 1));

        // Get the top-level window ancestor (may be null during early init — safe,
        // JWindow(null) falls back to a shared owner frame)
        Window owner = SwingUtilities.getWindowAncestor(SalesInvoiceGui.this);
        suggestionWindow = new JWindow(owner);
        // KEY: non-focusable → never steals focus from txtSearch
        suggestionWindow.setFocusableWindowState(false);
        suggestionWindow.add(scrollPane);
    }

    private void selectSuggestion(int index) {
        if (index < 0 || index >= suggestionModel.size()) {
            return;
        }
        SearchSuggestion suggestion = suggestionModel.get(index);
        suggestionWindow.setVisible(false);
        txtSearch.setText("");
        addProductToInvoice(suggestion.catalogProduct());
    }

    private void addProductToInvoice(CatalogProduct catalogProduct) {
        if (catalogProduct == null) {
            return;
        }
        if (catalogProduct.product.getCategory() == ProductCategory.ETC && !validatePrescription(false, true)) {
            return;
        }

        InvoiceLineState existing = findEditableLine(
                catalogProduct.product.getId(),
                catalogProduct.baseUnitOfMeasure.getMeasurement().getId()
        );
        if (existing != null) {
            int previousQuantity = existing.quantity;
            existing.quantity += 1;
            if (!rebuildNonGiftAllocations()) {
                existing.quantity = previousQuantity;
                rebuildNonGiftAllocations();
                showInsufficientInventory(catalogProduct.product.getName(), catalogProduct.baseUnitOfMeasure);
                return;
            }
        } else {
            InvoiceLineState newLine = new InvoiceLineState(
                    catalogProduct,
                    catalogProduct.baseUnitOfMeasure,
                    1,
                    resolveUnitPrice(catalogProduct, catalogProduct.baseUnitOfMeasure),
                    InvoiceLineType.SALE,
                    false
            );
            invoiceLines.add(newLine);
            if (!rebuildNonGiftAllocations()) {
                invoiceLines.remove(newLine);
                rebuildNonGiftAllocations();
                showInsufficientInventory(catalogProduct.product.getName(), catalogProduct.baseUnitOfMeasure);
                return;
            }
        }

        refreshPromotionChoices();
        refreshTableAndSummary();
    }

    /**
     * Rebuilds allocations for regular (non-gift) lines only, then re-applies
     * the selected promotion (which may add gift lines), then rebuilds all allocations.
     * Returns false only if the non-gift lines cannot be fulfilled.
     */
    private boolean rebuildNonGiftAllocations() {
        removeGiftLinesInternal();
        boolean ok = rebuildAllocationsForCurrentLines();
        if (!ok) {
            return false;
        }
        // Re-apply promotion gifts on top of the committed non-gift lines
        applySelectedPromotionIfPossible();
        // Rebuild allocations for all lines including gifts (failures here are silently ignored)
        rebuildAllocationsForCurrentLines();
        return true;
    }

    private boolean rebuildAllocationsForCurrentLines() {
        Map<String, Map<String, Integer>> remainingByProduct = buildRemainingLotQuantities();

        for (InvoiceLineState lineState : invoiceLines) {
            lineState.lotAllocations.clear();
            Map<String, Integer> remainingLots = remainingByProduct.getOrDefault(
                    lineState.catalogProduct.product.getId(),
                    Map.of()
            );
            int quantityNeeded = convertToBaseQuantity(lineState.quantity, lineState.selectedUnit);
            int remainingQuantity = quantityNeeded;

            for (LotDto lot : lineState.catalogProduct.availableLots) {
                Integer lotRemaining = remainingLots.get(lot.getId());
                if (lotRemaining == null || lotRemaining <= 0) {
                    continue;
                }
                int allocated = Math.min(lotRemaining, remainingQuantity);
                if (allocated > 0) {
                    lineState.lotAllocations.add(LotAllocationDto.builder()
                            .lotId(lot.getId())
                            .expiryDate(lot.getExpiryDate())
                            .status(lot.getStatus())
                            .quantity(allocated)
                            .invoiceLineType(lineState.lineType)
                            .productId(lineState.catalogProduct.product.getId())
                            .measurementId(lineState.selectedUnit.getMeasurement().getId())
                            .build());
                    remainingLots.put(lot.getId(), lotRemaining - allocated);
                    remainingQuantity -= allocated;
                }
                if (remainingQuantity == 0) {
                    break;
                }
            }

            if (remainingQuantity > 0) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Map<String, Integer>> buildRemainingLotQuantities() {
        Map<String, Map<String, Integer>> remainingByProduct = new HashMap<>();
        for (CatalogProduct catalogProduct : catalogProducts) {
            Map<String, Integer> lotQuantities = new LinkedHashMap<>();
            for (LotDto lot : catalogProduct.availableLots) {
                lotQuantities.put(lot.getId(), lot.getQuantity());
            }
            remainingByProduct.put(catalogProduct.product.getId(), lotQuantities);
        }
        return remainingByProduct;
    }

    private int convertToBaseQuantity(int quantity, UnitOfMeasureDto unitOfMeasure) {
        if (unitOfMeasure == null || unitOfMeasure.isBaseUnit()) {
            return quantity;
        }
        BigDecimal rate = unitOfMeasure.getBaseUnitConversionRate();
        if (rate == null || rate.signum() <= 0) {
            return quantity;
        }
        return BigDecimal.valueOf(quantity)
                .multiply(rate)
                .setScale(0, RoundingMode.CEILING)
                .intValue();
    }

    private BigDecimal resolveUnitPrice(CatalogProduct catalogProduct, UnitOfMeasureDto unitOfMeasure) {
        if (unitOfMeasure.getPrice() != null) {
            return unitOfMeasure.getPrice();
        }
        UnitOfMeasureDto baseUnit = catalogProduct.baseUnitOfMeasure;
        BigDecimal basePrice = baseUnit != null ? baseUnit.getPrice() : BigDecimal.ZERO;
        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }
        if (unitOfMeasure == null || unitOfMeasure.isBaseUnit()) {
            return basePrice;
        }
        BigDecimal rate = unitOfMeasure.getBaseUnitConversionRate();
        if (rate == null || rate.signum() <= 0) {
            return basePrice;
        }
        return basePrice.multiply(rate);
    }

    private void refreshPromotionChoices() {
        PromotionDto previouslySelected = getSelectedPromotionFromCombo();
        applicablePromotions.clear();

        for (PromotionDto promotion : promotionCatalog) {
            if (promotion == null || !promotion.isActive()) {
                continue;
            }
            if (!isPromotionWithinDateWindow(promotion) || !checkPromotionConditions(promotion)) {
                continue;
            }
            if (calculatePromotionDiscount(promotion).compareTo(BigDecimal.ZERO) > 0 || hasGiftAction(promotion)) {
                applicablePromotions.add(promotion);
            }
        }

        suppressPromotionEvents = true;
        promotionModel.removeAllElements();
        promotionModel.addElement("Không có khuyến mãi");
        for (PromotionDto promotion : applicablePromotions) {
            promotionModel.addElement(promotion);
        }

        if (previouslySelected != null) {
            PromotionDto matched = applicablePromotions.stream()
                    .filter(promotion -> Objects.equals(promotion.getId(), previouslySelected.getId()))
                    .findFirst()
                    .orElse(null);
            if (matched != null) {
                cmbPromotion.setSelectedItem(matched);
                selectedPromotion = matched;
            } else {
                cmbPromotion.setSelectedIndex(0);
                selectedPromotion = null;
            }
        } else {
            cmbPromotion.setSelectedIndex(0);
            selectedPromotion = null;
        }
        suppressPromotionEvents = false;
        applySelectedPromotionIfPossible();
    }

    private void applySelectedPromotionIfPossible() {
        removeGiftLinesInternal();

        PromotionDto comboSelection = getSelectedPromotionFromCombo();
        if (comboSelection == null) {
            selectedPromotion = null;
            return;
        }

        selectedPromotion = comboSelection;
        for (PromotionActionDto action : orderedPromotionActions(selectedPromotion)) {
            if (action.getType() != ActionType.PRODUCT_GIFT
                    || action.getProductId() == null
                    || action.getMeasurementId() == null) {
                continue;
            }

            CatalogProduct catalogProduct = catalogProductById.get(action.getProductId());
            UnitOfMeasureDto unitOfMeasure = catalogProduct == null ? null : catalogProduct.findByMeasurementId(action.getMeasurementId());
            if (catalogProduct == null || unitOfMeasure == null || action.getValue() == null || action.getValue().signum() <= 0) {
                continue;
            }

            InvoiceLineState giftLine = new InvoiceLineState(
                    catalogProduct,
                    unitOfMeasure,
                    action.getValue().intValue(),
                    BigDecimal.ZERO,
                    InvoiceLineType.GIFT,
                    true
            );
            invoiceLines.add(giftLine);
            if (!rebuildAllocationsForCurrentLines()) {
                invoiceLines.remove(giftLine);
                rebuildAllocationsForCurrentLines();
            }
        }
    }

    private void removeGiftLinesInternal() {
        invoiceLines.removeIf(lineState -> lineState.gift);
    }

    private void refreshTableAndSummary() {
        suppressTableEvents = true;
        invoiceTableModel.setRowCount(0);
        for (InvoiceLineState lineState : invoiceLines) {
            invoiceTableModel.addRow(new Object[]{
                    lineState.catalogProduct.product.getId(),
                    lineState.catalogProduct.product.getName(),
                    lineState.selectedUnit.getMeasurement().getName(),
                    lineState.quantity,
                    lineState.unitPrice,
                    lineState.calculateSubtotal()
            });
        }
        suppressTableEvents = false;

        txtVat.setText(currencyFormat.format(calculateVatAmount()));
        txtDiscount.setText(currencyFormat.format(calculateDiscountAmount()));
        txtTotal.setText(currencyFormat.format(calculateTotalAmount()));
        refreshCashButtons();

        if (rdoBank.isSelected()) {
            txtCustomerPayment.setValue(calculateTotalAmount().longValue());
        }
    }

    private BigDecimal calculateVatAmount() {
        return invoiceLines.stream()
                .map(InvoiceLineState::calculateVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscountAmount() {
        return selectedPromotion == null ? BigDecimal.ZERO : calculatePromotionDiscount(selectedPromotion);
    }

    private BigDecimal calculateTotalAmount() {
        BigDecimal subtotalWithVat = invoiceLines.stream()
                .map(InvoiceLineState::calculateTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotalWithVat.subtract(calculateDiscountAmount()).max(BigDecimal.ZERO);
    }

    private BigDecimal calculatePromotionDiscount(PromotionDto promotion) {
        if (promotion == null || !checkPromotionConditions(promotion)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        for (PromotionActionDto action : orderedPromotionActions(promotion)) {
            if (action.getTarget() == Target.ORDER_SUBTOTAL) {
                discount = discount.add(calculateOrderDiscount(action));
            } else if (action.getTarget() == Target.PRODUCT) {
                discount = discount.add(calculateProductDiscount(action));
            }
        }
        return discount;
    }

    private BigDecimal calculateOrderDiscount(PromotionActionDto action) {
        BigDecimal subtotalWithVat = invoiceLines.stream()
                .filter(lineState -> !lineState.gift)
                .map(InvoiceLineState::calculateTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (action.getType() == ActionType.PERCENT_DISCOUNT && action.getValue() != null) {
            return subtotalWithVat.multiply(action.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (action.getType() == ActionType.FIXED_DISCOUNT && action.getValue() != null) {
            return action.getValue();
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateProductDiscount(PromotionActionDto action) {
        BigDecimal discount = BigDecimal.ZERO;
        for (InvoiceLineState lineState : invoiceLines) {
            if (lineState.gift) {
                continue;
            }
            if (action.getProductId() != null && !Objects.equals(lineState.catalogProduct.product.getId(), action.getProductId())) {
                continue;
            }
            if (action.getMeasurementId() != null && !Objects.equals(lineState.selectedUnit.getMeasurement().getId(), action.getMeasurementId())) {
                continue;
            }

            if (action.getType() == ActionType.PERCENT_DISCOUNT && action.getValue() != null) {
                discount = discount.add(lineState.calculateTotalWithVat()
                        .multiply(action.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            } else if (action.getType() == ActionType.FIXED_DISCOUNT && action.getValue() != null) {
                discount = discount.add(action.getValue());
            }
        }
        return discount;
    }

    private boolean checkPromotionConditions(PromotionDto promotion) {
        if (promotion.getConditions() == null || promotion.getConditions().isEmpty()) {
            return true;
        }
        for (PromotionConditionDto condition : promotion.getConditions()) {
            if (!checkPromotionCondition(condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPromotionCondition(PromotionConditionDto condition) {
        if (condition == null) {
            return false;
        }
        if (condition.getTarget() == Target.ORDER_SUBTOTAL) {
            BigDecimal subtotalWithVat = invoiceLines.stream()
                    .filter(lineState -> !lineState.gift)
                    .map(InvoiceLineState::calculateTotalWithVat)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return compare(subtotalWithVat, condition.getValue(), condition.getComparator());
        }
        if (condition.getTarget() == Target.PRODUCT && condition.getType() == ConditionType.PRODUCT_QTY) {
            BigDecimal totalQuantity = invoiceLines.stream()
                    .filter(lineState -> !lineState.gift)
                    .filter(lineState -> Objects.equals(lineState.catalogProduct.product.getId(), condition.getProductId()))
                    .filter(lineState -> condition.getMeasurementId() == null
                            || Objects.equals(lineState.selectedUnit.getMeasurement().getId(), condition.getMeasurementId()))
                    .map(lineState -> BigDecimal.valueOf(lineState.quantity))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return compare(totalQuantity, condition.getValue(), condition.getComparator());
        }
        if (condition.getTarget() == Target.PRODUCT && condition.getType() == ConditionType.PRODUCT_ID) {
            boolean exists = invoiceLines.stream()
                    .filter(lineState -> !lineState.gift)
                    .anyMatch(lineState -> Objects.equals(lineState.catalogProduct.product.getId(), condition.getProductId()));
            return compare(exists ? BigDecimal.ONE : BigDecimal.ZERO, condition.getValue(), condition.getComparator());
        }
        return false;
    }

    private boolean compare(BigDecimal actual, BigDecimal expected, Comparator comparator) {
        if (comparator == null) {
            return false;
        }
        int compare = actual.compareTo(expected == null ? BigDecimal.ZERO : expected);
        return switch (comparator) {
            case GREATER_EQUAL -> compare >= 0;
            case LESS_EQUAL -> compare <= 0;
            case GREATER -> compare > 0;
            case LESS -> compare < 0;
            case EQUAL -> compare == 0;
        };
    }

    private boolean isPromotionWithinDateWindow(PromotionDto promotion) {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = promotion.getEffectiveDate() == null || !now.isBefore(promotion.getEffectiveDate());
        boolean beforeEnd = promotion.getEndDate() == null || !now.isAfter(promotion.getEndDate());
        return afterStart && beforeEnd;
    }

    private boolean hasGiftAction(PromotionDto promotion) {
        if (promotion == null || promotion.getActions() == null) {
            return false;
        }
        return promotion.getActions().stream().anyMatch(action -> action.getType() == ActionType.PRODUCT_GIFT);
    }

    private List<PromotionActionDto> orderedPromotionActions(PromotionDto promotion) {
        if (promotion == null || promotion.getActions() == null) {
            return List.of();
        }
        return promotion.getActions().stream()
                .sorted(java.util.Comparator.comparingInt(PromotionActionDto::getActionOrder))
                .toList();
    }

    private InvoiceLineState findEditableLine(String productId, String measurementId) {
        for (InvoiceLineState lineState : invoiceLines) {
            if (!lineState.gift
                    && Objects.equals(lineState.catalogProduct.product.getId(), productId)
                    && Objects.equals(lineState.selectedUnit.getMeasurement().getId(), measurementId)) {
                return lineState;
            }
        }
        return null;
    }

    private void handleTableUpdate(int row) {
        if (row < 0 || row >= invoiceLines.size()) {
            return;
        }

        InvoiceLineState lineState = invoiceLines.get(row);
        if (lineState.gift) {
            return;
        }

        UnitOfMeasureDto previousUnit = lineState.selectedUnit;
        int previousQuantity = lineState.quantity;

        String unitName = Objects.toString(invoiceTableModel.getValueAt(row, 2), "");
        int quantity = safeInteger(invoiceTableModel.getValueAt(row, 3), 1);
        if (quantity < 1) {
            quantity = 1;
        }

        UnitOfMeasureDto selectedUnit = lineState.catalogProduct.findByMeasurementName(unitName);
        if (selectedUnit == null) {
            selectedUnit = previousUnit;
        }

        lineState.selectedUnit = selectedUnit;
        lineState.quantity = quantity;
        lineState.unitPrice = resolveUnitPrice(lineState.catalogProduct, selectedUnit);

        if (!rebuildNonGiftAllocations()) {
            lineState.selectedUnit = previousUnit;
            lineState.quantity = previousQuantity;
            lineState.unitPrice = resolveUnitPrice(lineState.catalogProduct, previousUnit);
            rebuildNonGiftAllocations();
            showInsufficientInventory(lineState.catalogProduct.product.getName(), selectedUnit);
        }

        refreshPromotionChoices();
        refreshTableAndSummary();
    }

    private void removeAllItems() {
        if (invoiceLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bảng chi tiết hóa đơn đang trống.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        invoiceLines.clear();
        selectedPromotion = null;
        cmbPromotion.setSelectedIndex(0);
        refreshPromotionChoices();
        refreshTableAndSummary();
    }

    private void removeSelectedItems() {
        int[] selectedRows = tblInvoiceLines.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có sản phẩm nào được chọn.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int index = selectedRows.length - 1; index >= 0; index--) {
            int modelRow = tblInvoiceLines.convertRowIndexToModel(selectedRows[index]);
            if (modelRow >= 0 && modelRow < invoiceLines.size()) {
                invoiceLines.remove(modelRow);
            }
        }

        rebuildNonGiftAllocations();
        refreshPromotionChoices();
        refreshTableAndSummary();
    }

    private boolean validatePrescription(boolean showMessage, boolean requestFocus) {
        String prescriptionCode = txtPrescriptionCode.getText().trim();
        boolean hasEtc = invoiceLines.stream()
                .filter(lineState -> !lineState.gift)
                .anyMatch(lineState -> lineState.catalogProduct.product.getCategory() == ProductCategory.ETC);

        if (prescriptionCode.isBlank()) {
            // Only warn if we actually have ETC products AND the caller explicitly wants a message
            if (hasEtc && showMessage) {
                JOptionPane.showMessageDialog(this, "Hóa đơn có thuốc ETC, vui lòng nhập mã đơn kê thuốc.", "Thiếu mã đơn kê thuốc", JOptionPane.WARNING_MESSAGE);
            }
            if (hasEtc && requestFocus) {
                txtPrescriptionCode.requestFocusInWindow();
            }
            return !hasEtc;
        }

        if (!PRESCRIPTION_PATTERN.matcher(prescriptionCode).matches()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                        "Mã đơn kê thuốc không đúng định dạng.\nĐịnh dạng: XXXXXYYYYYYY-Z\n(5 mã cơ sở + 7 mã đơn + ký tự loại N/H/C)",
                        "Sai định dạng", JOptionPane.ERROR_MESSAGE);
            }
            if (requestFocus) {
                SwingUtilities.invokeLater(() -> txtPrescriptionCode.requestFocusInWindow());
            }
            return false;
        }
        return true;
    }

    private boolean validateCustomerPhone() {
        String phoneNumber = txtCustomerPhone.getText().trim();
        return phoneNumber.isBlank() || PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    private void refreshCashButtons() {
        pnlCashOptions.removeAll();
        long roundedTotal = calculateTotalAmount()
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.CEILING)
                .multiply(BigDecimal.valueOf(1000))
                .longValue();

        long[] amounts = {1000L, 2000L, 5000L, 10000L, 20000L, 50000L, 100000L, 200000L, 500000L, roundedTotal};
        for (long amount : amounts) {
            JButton button = createActionButton(currencyFormat.format(amount), "cash_" + amount);
            pnlCashOptions.add(button);
        }
        pnlCashOptions.revalidate();
        pnlCashOptions.repaint();
    }

    private void handleCashSelection() {
        pnlCashOptions.setVisible(true);
        txtCustomerPayment.setEnabled(true);
        txtCustomerPayment.setEditable(true);
        if (txtCustomerPayment.getValue() == null) {
            txtCustomerPayment.setValue(0L);
        }
    }

    private void handleBankSelection() {
        pnlCashOptions.setVisible(false);
        txtCustomerPayment.setEditable(false);
        txtCustomerPayment.setEnabled(false);
        txtCustomerPayment.setValue(calculateTotalAmount().longValue());
    }

    private void addCashAmount(long amount) {
        long currentValue = safeLong(txtCustomerPayment.getValue(), 0L);
        txtCustomerPayment.setValue(currentValue + amount);
    }

    private void processPayment() {
        if (currentShift == null) {
            JOptionPane.showMessageDialog(this, "Chưa có ca làm việc đang mở nên chưa thể thanh toán.", "Thiếu ca làm việc", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (invoiceLines.stream().noneMatch(lineState -> !lineState.gift)) {
            JOptionPane.showMessageDialog(this, "Danh sách hóa đơn đang trống.", "Không thể thanh toán", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validatePrescription(true, true)) {
            return;
        }
        if (!validateCustomerPhone()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại khách hàng không đúng định dạng 10 số bắt đầu bằng 0.", "Sai định dạng", JOptionPane.WARNING_MESSAGE);
            txtCustomerPhone.requestFocusInWindow();
            return;
        }
        if (rdoBank.isSelected()) {
            JOptionPane.showMessageDialog(this, "Thanh toán ngân hàng/ví điện tử tạm thời chưa được hiện thực.", "Chưa hỗ trợ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        long customerPayment = safeLong(txtCustomerPayment.getValue(), 0L);
        long totalAmount = calculateTotalAmount().longValue();
        if (customerPayment < totalAmount) {
            JOptionPane.showMessageDialog(this, "Tiền khách đưa phải lớn hơn hoặc bằng tổng tiền.", "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InvoiceDto invoiceDto = buildInvoiceDto();
        btnProcessPayment.setEnabled(false);

        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return networkService.send(CommandType.INVOICE_CREATE, invoiceDto);
            }

            @Override
            protected void done() {
                btnProcessPayment.setEnabled(true);
                try {
                    Response response = get();
                    if (response != null && response.isSuccess()) {
                        long change = safeLong(txtCustomerPayment.getValue(), 0L) - calculateTotalAmount().longValue();
                        String msg = "Thanh toán thành công!";
                        if (rdoCash.isSelected() && change > 0) {
                            msg += "\nTiền thừa trả khách: " + currencyFormat.format(change);
                        }
                        JOptionPane.showMessageDialog(SalesInvoiceGui.this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        resetInvoiceForm();
                        // Reload catalog + shift after successful payment
                        loadInitialData();
                    } else {
                        showError(response == null ? "Không có phản hồi từ server." : response.getMessage());
                    }
                } catch (Exception exception) {
                    showError("Không thể tạo hóa đơn: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private InvoiceDto buildInvoiceDto() {
        List<InvoiceLineDto> lineDtos = new ArrayList<>();
        for (InvoiceLineState lineState : invoiceLines) {
            List<LotAllocationDto> allocationDtos = lineState.lotAllocations.stream()
                    .map(allocation -> LotAllocationDto.builder()
                            .lotId(allocation.getLotId())
                            .expiryDate(allocation.getExpiryDate())
                            .status(allocation.getStatus())
                            .quantity(allocation.getQuantity())
                            .invoiceLineType(lineState.lineType)
                            .productId(lineState.catalogProduct.product.getId())
                            .measurementId(lineState.selectedUnit.getMeasurement().getId())
                            .build())
                    .toList();

            lineDtos.add(InvoiceLineDto.builder()
                    .productId(lineState.catalogProduct.product.getId())
                    .measurementId(lineState.selectedUnit.getMeasurement().getId())
                    .measurementName(lineState.selectedUnit.getMeasurement().getName())
                    .type(lineState.lineType)
                    .unitPrice(lineState.unitPrice)
                    .quantity(lineState.quantity)
                    .lotAllocations(allocationDtos)
                    .build());
        }

        String prescriptionCode = txtPrescriptionCode.getText().trim();
        String phoneNumber = txtCustomerPhone.getText().trim();

        return InvoiceDto.builder()
                .type(InvoiceType.SALE)
                .creatorId(currentStaff.getId())
                .creatorFullName(currentStaff.getFullName())
                .shiftId(currentShift.getId())
                .prescriptionCode(prescriptionCode.isBlank() ? null : prescriptionCode)
                .customerPhoneNumber(phoneNumber.isBlank() ? null : phoneNumber)
                .invoiceLines(lineDtos)
                .promotionId(selectedPromotion != null ? selectedPromotion.getId() : null)
                .promotionName(selectedPromotion != null ? selectedPromotion.getName() : null)
                .paymentMethod(PaymentMethod.CASH_PAYMENT)
                .build();
    }

    private void resetInvoiceForm() {
        invoiceLines.clear();
        selectedPromotion = null;
        txtPrescriptionCode.setText("");
        txtCustomerPhone.setText("");
        txtCustomerPayment.setValue(0L);
        rdoCash.setSelected(true);
        handleCashSelection();
        refreshPromotionChoices();
        refreshTableAndSummary();
    }

    private void showInsufficientInventory(String productName, UnitOfMeasureDto unitOfMeasure) {
        String unitName = unitOfMeasure != null && unitOfMeasure.getMeasurement() != null
                ? unitOfMeasure.getMeasurement().getName()
                : "đơn vị đã chọn";
        JOptionPane.showMessageDialog(
                this,
                "Không đủ tồn kho cho sản phẩm \"" + productName + "\" ở đơn vị \"" + unitName + "\".",
                "Không đủ tồn kho",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private DecimalFormat createCurrencyFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0 'Đ'", symbols);
        format.setGroupingUsed(true);
        format.setGroupingSize(3);
        return format;
    }

    private PromotionDto getSelectedPromotionFromCombo() {
        Object selectedItem = cmbPromotion.getSelectedItem();
        return selectedItem instanceof PromotionDto promotion ? promotion : null;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private int safeInteger(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private long safeLong(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    private String getCurrentWorkstation() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "UNKNOWN_WORKSTATION";
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnRemoveAll) {
            removeAllItems();
            return;
        }
        if (source == btnRemoveSelected) {
            removeSelectedItems();
            return;
        }
        if (source == btnProcessPayment) {
            processPayment();
            return;
        }
        if (source == rdoCash) {
            handleCashSelection();
            return;
        }
        if (source == rdoBank) {
            handleBankSelection();
            return;
        }
        if (source == cmbPromotion) {
            if (!suppressPromotionEvents) {
                selectedPromotion = getSelectedPromotionFromCombo();
                applySelectedPromotionIfPossible();
                refreshTableAndSummary();
            }
            return;
        }
        if (source instanceof JButton button && button.getName() != null && button.getName().startsWith("cash_")) {
            addCashAmount(Long.parseLong(button.getName().substring(5)));
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        if (event.getDocument() == txtSearch.getDocument()) {
            SwingUtilities.invokeLater(this::updateSearchSuggestions);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        insertUpdate(event);
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        insertUpdate(event);
    }

    @Override
    public void focusGained(FocusEvent event) {
    }

    @Override
    public void focusLost(FocusEvent event) {
        if (event.getSource() == txtPrescriptionCode) {
            // Only show warning if invoice actually has ETC products
            boolean hasEtc = invoiceLines.stream()
                    .filter(ls -> !ls.gift)
                    .anyMatch(ls -> ls.catalogProduct.product.getCategory() == ProductCategory.ETC);
            if (hasEtc) {
                validatePrescription(true, false);
            }
        } else if (event.getSource() == txtSearch) {
            // The suggestionWindow is non-focusable, so focusLost only fires here
            // when the user genuinely clicks away from txtSearch — safe to hide.
            if (suggestionWindow != null) {
                suggestionWindow.setVisible(false);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getSource() != txtSearch
                || suggestionWindow == null
                || !suggestionWindow.isVisible()
                || suggestionModel == null
                || suggestionModel.isEmpty()) {
            return;
        }

        int selectedIndex = suggestionList.getSelectedIndex();
        switch (event.getKeyCode()) {
            case KeyEvent.VK_DOWN -> {
                suggestionList.setSelectedIndex((selectedIndex + 1) % suggestionModel.size());
                suggestionList.ensureIndexIsVisible(suggestionList.getSelectedIndex());
                event.consume();
            }
            case KeyEvent.VK_UP -> {
                suggestionList.setSelectedIndex((selectedIndex + suggestionModel.size() - 1) % suggestionModel.size());
                suggestionList.ensureIndexIsVisible(suggestionList.getSelectedIndex());
                event.consume();
            }
            case KeyEvent.VK_ENTER -> {
                selectSuggestion(suggestionList.getSelectedIndex());
                event.consume();
            }
            case KeyEvent.VK_ESCAPE -> suggestionWindow.setVisible(false);
            default -> {
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() == suggestionList && suggestionList.getSelectedIndex() >= 0) {
            selectSuggestion(suggestionList.getSelectedIndex());
        }
    }

    @Override public void mousePressed(MouseEvent event) {}
    @Override public void mouseReleased(MouseEvent event) {}
    @Override public void mouseEntered(MouseEvent event) {}
    @Override public void mouseExited(MouseEvent event) {}
    @Override public void valueChanged(ListSelectionEvent event) {}

    @Override
    public void tableChanged(TableModelEvent event) {
        if (suppressTableEvents || event.getType() != TableModelEvent.UPDATE) {
            return;
        }
        if (event.getColumn() == 2 || event.getColumn() == 3) {
            handleTableUpdate(event.getFirstRow());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("tableCellEditor".equals(event.getPropertyName())) {
            if (event.getNewValue() instanceof TableCellEditor) {
                // Editor just started — record which row is being edited
                lastEditedRow = tblInvoiceLines.getEditingRow();
            } else if (event.getOldValue() instanceof TableCellEditor && event.getNewValue() == null) {
                // Editor just stopped — use lastEditedRow (editingRow is -1 at this point)
                int row = lastEditedRow >= 0 ? lastEditedRow : tblInvoiceLines.getSelectedRow();
                lastEditedRow = -1;
                if (row >= 0) {
                    handleTableUpdate(row);
                }
            }
        }
    }

    private static final class DataBundle {
        private final List<ProductDto> products;
        private final List<LotDto> lots;
        private final List<PromotionDto> promotions;
        private final ShiftDto activeShift;

        private DataBundle(List<ProductDto> products, List<LotDto> lots, List<PromotionDto> promotions, ShiftDto activeShift) {
            this.products = products;
            this.lots = lots;
            this.promotions = promotions;
            this.activeShift = activeShift;
        }

        private List<ProductDto> products() { return products; }
        private List<LotDto> lots() { return lots; }
        private List<PromotionDto> promotions() { return promotions; }
        private ShiftDto activeShift() { return activeShift; }
    }

    private static final class CatalogProduct {
        private final ProductDto product;
        private final List<LotDto> availableLots;
        private final List<UnitOfMeasureDto> unitOfMeasures;
        private final UnitOfMeasureDto baseUnitOfMeasure;
        private final Map<String, UnitOfMeasureDto> byMeasurementId = new HashMap<>();
        private final Map<String, UnitOfMeasureDto> byMeasurementName = new HashMap<>();

        private CatalogProduct(ProductDto product, List<LotDto> availableLots) {
            this.product = product;
            this.availableLots = new ArrayList<>(availableLots);
            this.unitOfMeasures = product.getUnitOfMeasures() == null ? List.of() : product.getUnitOfMeasures();

            UnitOfMeasureDto baseUnit = null;
            for (UnitOfMeasureDto unitOfMeasure : unitOfMeasures) {
                if (unitOfMeasure.getMeasurement() != null) {
                    byMeasurementId.put(unitOfMeasure.getMeasurement().getId(), unitOfMeasure);
                    byMeasurementName.put(unitOfMeasure.getMeasurement().getName(), unitOfMeasure);
                }
                if (unitOfMeasure.isBaseUnit()) {
                    baseUnit = unitOfMeasure;
                }
            }
            this.baseUnitOfMeasure = baseUnit;
        }

        private UnitOfMeasureDto findByMeasurementId(String measurementId) {
            return byMeasurementId.get(measurementId);
        }

        private UnitOfMeasureDto findByMeasurementName(String measurementName) {
            return byMeasurementName.get(measurementName);
        }
    }

    private static final class InvoiceLineState {
        private final CatalogProduct catalogProduct;
        private UnitOfMeasureDto selectedUnit;
        private int quantity;
        private BigDecimal unitPrice;
        private final InvoiceLineType lineType;
        private final boolean gift;
        private final List<LotAllocationDto> lotAllocations = new ArrayList<>();

        private InvoiceLineState(
                CatalogProduct catalogProduct,
                UnitOfMeasureDto selectedUnit,
                int quantity,
                BigDecimal unitPrice,
                InvoiceLineType lineType,
                boolean gift
        ) {
            this.catalogProduct = catalogProduct;
            this.selectedUnit = selectedUnit;
            this.quantity = quantity;
            this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
            this.lineType = lineType;
            this.gift = gift;
        }

        private BigDecimal calculateSubtotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        private BigDecimal calculateVatAmount() {
            BigDecimal vat = catalogProduct.product.getVat() == null ? BigDecimal.ZERO : catalogProduct.product.getVat();
            return calculateSubtotal()
                    .multiply(vat)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        private BigDecimal calculateTotalWithVat() {
            return calculateSubtotal().add(calculateVatAmount());
        }
    }

    private static final class SearchSuggestion {
        private final CatalogProduct catalogProduct;

        private SearchSuggestion(CatalogProduct catalogProduct) {
            this.catalogProduct = catalogProduct;
        }

        private CatalogProduct catalogProduct() {
            return catalogProduct;
        }

        @Override
        public String toString() {
            ProductDto product = catalogProduct.product;
            return product.getId() + " - " + product.getName() + " - " + product.getShortName();
        }
    }

    private final class PromotionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof PromotionDto promotion) {
                StringBuilder label = new StringBuilder(promotion.getName());
                BigDecimal discount = calculatePromotionDiscount(promotion);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    label.append(" (Giảm ").append(currencyFormat.format(discount)).append(")");
                }
                if (hasGiftAction(promotion)) {
                    label.append(" (Có quà tặng)");
                }
                setText(label.toString());
            }
            return this;
        }
    }

    private final class CurrencyRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            if (value instanceof BigDecimal bigDecimal) {
                setText(currencyFormat.format(bigDecimal));
            } else {
                super.setValue(value);
            }
        }
    }

    private final class UnitOfMeasureCellEditor extends DefaultCellEditor {
        private final JComboBox<String> comboBox;

        @SuppressWarnings("unchecked")
        private UnitOfMeasureCellEditor() {
            super(new JComboBox<String>());
            comboBox = (JComboBox<String>) getComponent();
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Populate with this specific row's UOMs BEFORE calling super
            // (super would reset the combobox contents)
            comboBox.removeAllItems();
            if (row >= 0 && row < invoiceLines.size()) {
                InvoiceLineState lineState = invoiceLines.get(row);
                for (UnitOfMeasureDto unitOfMeasure : lineState.catalogProduct.unitOfMeasures) {
                    if (unitOfMeasure.getMeasurement() != null) {
                        comboBox.addItem(unitOfMeasure.getMeasurement().getName());
                    }
                }
            }
            // Set the current value directly on our comboBox — do NOT call super
            // because DefaultCellEditor.getTableCellEditorComponent() would replace
            // the combobox model with the delegate's original (empty) model.
            if (value != null) {
                comboBox.setSelectedItem(value);
            }
            return comboBox;
        }
    }

    private final class QuantitySpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        private QuantitySpinnerEditor() {
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(safeInteger(value, 1));
            return spinner;
        }
    }
}
