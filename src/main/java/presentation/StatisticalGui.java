package presentation;

import core.dto.InvoiceDto;
import core.dto.InvoiceLineDto;
import core.dto.LotDto;
import core.dto.ProductDto;
import core.entities.enums.InvoiceLineType;
import core.entities.enums.InvoiceType;
import infrastructure.network.CommandType;
import infrastructure.network.NetworkService;
import infrastructure.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StatisticalGui extends JPanel {
    private static final Color CLR_BG = AppColors.BACKGROUND;
    private static final Color CLR_WHITE = AppColors.WHITE;
    private static final Color CLR_ACCENT = AppColors.DARK;
    private static final Color CLR_PRIMARY = AppColors.PRIMARY;
    private static final Color CLR_SUCCESS = AppColors.SUCCESS;
    private static final Color CLR_DANGER = AppColors.DANGER;
    private static final Color CLR_WARNING = AppColors.WARNING;
    private static final Color CLR_BORDER = AppColors.LIGHT;
    private static final Color CLR_MUTED = AppColors.PLACEHOLDER_TEXT;
    private static final Color CLR_ROW_EVEN = new Color(0xEEF7FA);
    private static final Color CLR_ROW_SEL = new Color(0xB3E0F0);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_CARD_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 13);

    private final NetworkService networkService;

    private JLabel lblTodayRevenue;
    private JLabel lblMonthRevenue;
    private JLabel lblTodayOrders;
    private JLabel lblLowStock;

    private JLabel lblStatus;
    private JButton btnReload;

    private DefaultTableModel mdlRevenueByDay;
    private DefaultTableModel mdlTopProducts;
    private DefaultTableModel mdlLowStock;

    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public StatisticalGui(NetworkService networkService) {
        this.networkService = networkService;
        setLayout(new BorderLayout());
        setBackground(CLR_BG);
        initComponents();
        loadStatistics();
    }

    private void initComponents() {
        add(buildTopBar(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(CLR_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, CLR_BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("Thống kê & Báo cáo");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_ACCENT);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(CLR_WHITE);

        lblStatus = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblStatus.setFont(FONT_CARD_LABEL);
        lblStatus.setForeground(CLR_MUTED);
        right.add(lblStatus);

        btnReload = makeButton("Tải lại", CLR_PRIMARY);
        btnReload.addActionListener(e -> loadStatistics());
        right.add(btnReload);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        body.add(buildKpiRow());
        body.add(Box.createVerticalStrut(16));
        body.add(buildRevenueTableCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildBottomTables());

        return body;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(CLR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblTodayRevenue = new JLabel("0 đ");
        lblMonthRevenue = new JLabel("0 đ");
        lblTodayOrders = new JLabel("0");
        lblLowStock = new JLabel("0 sản phẩm");

        row.add(buildKpiCard("Doanh thu hôm nay", lblTodayRevenue, "Tính từ hóa đơn bán", CLR_PRIMARY));
        row.add(buildKpiCard("Doanh thu tháng này", lblMonthRevenue, "Tính từ ngày đầu tháng", CLR_SUCCESS));
        row.add(buildKpiCard("Đơn hàng hôm nay", lblTodayOrders, "Số hóa đơn SALE", CLR_WARNING));
        row.add(buildKpiCard("Tồn kho thấp", lblLowStock, "Tồn dưới 50", CLR_DANGER));

        return row;
    }

    private JPanel buildKpiCard(String label, JLabel valueLabel, String sub, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel accent = new JPanel();
        accent.setBackground(color);
        accent.setPreferredSize(new Dimension(4, 0));
        card.add(accent, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 2));
        content.setBackground(CLR_WHITE);
        content.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(CLR_MUTED);

        valueLabel.setFont(FONT_CARD_VALUE);
        valueLabel.setForeground(CLR_ACCENT);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(FONT_CARD_LABEL);
        lblSub.setForeground(color);

        content.add(lbl);
        content.add(valueLabel);
        content.add(lblSub);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRevenueTableCard() {
        mdlRevenueByDay = new DefaultTableModel(
                new String[]{"Ngày", "Số hóa đơn", "Doanh thu"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(mdlRevenueByDay);
        styleTable(table);
        alignRight(table, 1);
        alignRight(table, 2);

        return buildTableCard("Doanh thu 7 ngày gần nhất", table);
    }

    private JPanel buildBottomTables() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(CLR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        mdlTopProducts = new DefaultTableModel(
                new String[]{"Sản phẩm", "Đã bán", "Doanh thu"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable topTable = new JTable(mdlTopProducts);
        styleTable(topTable);
        alignRight(topTable, 1);
        alignRight(topTable, 2);
        row.add(buildTableCard("Sản phẩm bán chạy", topTable));

        mdlLowStock = new DefaultTableModel(
                new String[]{"Mã SP", "Tên sản phẩm", "Tồn kho"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable stockTable = new JTable(mdlLowStock);
        styleTable(stockTable);
        alignRight(stockTable, 2);
        row.add(buildTableCard("Tồn kho thấp", stockTable));

        return row;
    }

    private JPanel buildTableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(CLR_ACCENT);
        card.add(lblTitle, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadStatistics() {
        setLoading(true);

        new SwingWorker<StatisticData, Void>() {
            @Override
            protected StatisticData doInBackground() throws Exception {
                List<InvoiceDto> invoices = loadList(CommandType.INVOICE_LOAD_ALL, InvoiceDto.class);
                List<ProductDto> products = loadList(CommandType.PRODUCT_LOAD_ALL, ProductDto.class);
                List<LotDto> lots = loadList(CommandType.LOT_LOAD_ALL, LotDto.class);
                return new StatisticData(invoices, products, lots);
            }

            @Override
            protected void done() {
                try {
                    renderStatistics(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError("Tải thống kê bị gián đoạn.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    showError("Không thể tải thống kê: " + (cause == null ? e.getMessage() : cause.getMessage()));
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private <T> List<T> loadList(CommandType command, Class<T> type) throws Exception {
        Response response = networkService.send(command, null);
        if (response == null || !response.isSuccess()) {
            throw new IllegalStateException(response == null ? "Server không phản hồi." : response.getMessage());
        }

        List<T> result = new ArrayList<>();
        if (response.getData() instanceof List<?> list) {
            for (Object obj : list) {
                if (type.isInstance(obj)) {
                    result.add(type.cast(obj));
                }
            }
        }
        return result;
    }

    private void renderStatistics(StatisticData data) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        Map<String, ProductDto> productById = new HashMap<>();
        for (ProductDto p : data.products()) {
            productById.put(p.getId(), p);
        }

        BigDecimal todayRevenue = BigDecimal.ZERO;
        BigDecimal monthRevenue = BigDecimal.ZERO;
        int todayOrders = 0;

        Map<LocalDate, BigDecimal> revenueByDay = new TreeMap<>();
        Map<LocalDate, Integer> ordersByDay = new TreeMap<>();
        Map<String, Integer> soldQtyByProduct = new HashMap<>();
        Map<String, BigDecimal> revenueByProduct = new HashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            revenueByDay.put(day, BigDecimal.ZERO);
            ordersByDay.put(day, 0);
        }

        for (InvoiceDto invoice : data.invoices()) {
            if (invoice.getType() != InvoiceType.SALE) continue;
            if (invoice.getCreationDate() == null) continue;

            LocalDate invoiceDate = invoice.getCreationDate().toLocalDate();
            BigDecimal invoiceTotal = calculateInvoiceTotal(invoice);

            if (invoiceDate.equals(today)) {
                todayRevenue = todayRevenue.add(invoiceTotal);
                todayOrders++;
            }

            if (!invoiceDate.isBefore(firstDayOfMonth) && !invoiceDate.isAfter(today)) {
                monthRevenue = monthRevenue.add(invoiceTotal);
            }

            if (revenueByDay.containsKey(invoiceDate)) {
                revenueByDay.put(invoiceDate, revenueByDay.get(invoiceDate).add(invoiceTotal));
                ordersByDay.put(invoiceDate, ordersByDay.get(invoiceDate) + 1);
            }

            if (invoice.getInvoiceLines() != null) {
                for (InvoiceLineDto line : invoice.getInvoiceLines()) {
                    if (line.getType() == InvoiceLineType.GIFT) continue;
                    if (line.getProductId() == null) continue;

                    BigDecimal lineRevenue = calculateLineTotal(line);
                    soldQtyByProduct.merge(line.getProductId(), line.getQuantity(), Integer::sum);
                    revenueByProduct.merge(line.getProductId(), lineRevenue, BigDecimal::add);
                }
            }
        }

        lblTodayRevenue.setText(formatMoney(todayRevenue) + " đ");
        lblMonthRevenue.setText(formatMoney(monthRevenue) + " đ");
        lblTodayOrders.setText(String.valueOf(todayOrders));

        fillRevenueTable(revenueByDay, ordersByDay);
        fillTopProductsTable(productById, soldQtyByProduct, revenueByProduct);
        fillLowStockTable(data.products(), data.lots());

        lblStatus.setText("Cập nhật: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
    }

    private BigDecimal calculateInvoiceTotal(InvoiceDto invoice) {
        BigDecimal total = BigDecimal.ZERO;
        if (invoice.getInvoiceLines() == null) return total;

        for (InvoiceLineDto line : invoice.getInvoiceLines()) {
            if (line.getType() == InvoiceLineType.GIFT) continue;
            total = total.add(calculateLineTotal(line));
        }
        return total;
    }

    private BigDecimal calculateLineTotal(InvoiceLineDto line) {
        if (line.getUnitPrice() == null) return BigDecimal.ZERO;
        return line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
    }

    private void fillRevenueTable(Map<LocalDate, BigDecimal> revenueByDay, Map<LocalDate, Integer> ordersByDay) {
        mdlRevenueByDay.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LocalDate day : revenueByDay.keySet()) {
            mdlRevenueByDay.addRow(new Object[]{
                    day.format(fmt),
                    ordersByDay.getOrDefault(day, 0),
                    formatMoney(revenueByDay.get(day)) + " đ"
            });
        }
    }

    private void fillTopProductsTable(
            Map<String, ProductDto> productById,
            Map<String, Integer> soldQtyByProduct,
            Map<String, BigDecimal> revenueByProduct
    ) {
        mdlTopProducts.setRowCount(0);

        List<String> productIds = new ArrayList<>(soldQtyByProduct.keySet());
        productIds.sort((a, b) -> Integer.compare(
                soldQtyByProduct.getOrDefault(b, 0),
                soldQtyByProduct.getOrDefault(a, 0)
        ));

        int count = 0;
        for (String productId : productIds) {
            if (count >= 10) break;

            ProductDto p = productById.get(productId);
            String name = p == null ? productId : p.getName();

            mdlTopProducts.addRow(new Object[]{
                    name,
                    soldQtyByProduct.getOrDefault(productId, 0),
                    formatMoney(revenueByProduct.getOrDefault(productId, BigDecimal.ZERO)) + " đ"
            });
            count++;
        }
    }

    private void fillLowStockTable(List<ProductDto> products, List<LotDto> lots) {
        mdlLowStock.setRowCount(0);

        Map<String, Integer> stockByProduct = new HashMap<>();
        for (LotDto lot : lots) {
            if (lot.getProductId() != null) {
                stockByProduct.merge(lot.getProductId(), lot.getQuantity(), Integer::sum);
            }
        }

        List<ProductDto> sorted = new ArrayList<>(products);
        sorted.sort(Comparator.comparingInt(p -> stockByProduct.getOrDefault(p.getId(), 0)));

        int lowStockCount = 0;
        for (ProductDto p : sorted) {
            int stock = stockByProduct.getOrDefault(p.getId(), 0);
            if (stock < 50) {
                lowStockCount++;
                mdlLowStock.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        stock
                });
            }
        }

        lblLowStock.setText(lowStockCount + " sản phẩm");
    }

    private JButton makeButton(String text, Color bg) {
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

    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(CLR_ROW_SEL);
        table.setSelectionForeground(CLR_ACCENT);
        table.setFocusable(false);

        table.getTableHeader().setFont(FONT_TABLE_HEADER);
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
                    if (val instanceof Integer v && v < 30) {
                        setForeground(CLR_DANGER);
                    } else {
                        setForeground(CLR_ACCENT);
                    }
                }
                return this;
            }
        });
    }

    private void alignRight(JTable table, int col) {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        if (col >= 0 && col < table.getColumnCount()) {
            table.getColumnModel().getColumn(col).setCellRenderer(right);
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0";
        return moneyFormat.format(value);
    }

    private void setLoading(boolean loading) {
        btnReload.setEnabled(!loading);
        btnReload.setText(loading ? "Đang tải..." : "Tải lại");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private record StatisticData(
            List<InvoiceDto> invoices,
            List<ProductDto> products,
            List<LotDto> lots
    ) {}
}