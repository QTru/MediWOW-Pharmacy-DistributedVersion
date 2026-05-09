package presentation;

import infrastructure.network.NetworkService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * StatisticalGui — JPanel thống kê doanh thu và sản phẩm.
 * Nhúng vào content area của MainMenuGui giống ProductGui.
 *
 * Gồm:
 *  - Hàng KPI cards (Doanh thu hôm nay, tháng này, đơn hàng, cảnh báo tồn kho)
 *  - Biểu đồ doanh thu (Java2D, không cần thư viện ngoài)
 *  - Bảng sản phẩm bán chạy + bảng tồn kho thấp
 *
 * TODO: Thay mock data bằng NetworkService.send(CommandType.STATISTIC_*, ...)
 */
public class StatisticalGui extends JPanel {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color CLR_BG       = AppColors.BACKGROUND;
    private static final Color CLR_WHITE    = AppColors.WHITE;
    private static final Color CLR_ACCENT   = AppColors.DARK;
    private static final Color CLR_PRIMARY  = AppColors.PRIMARY;
    private static final Color CLR_MUTED    = AppColors.PLACEHOLDER_TEXT;
    private static final Color CLR_BORDER   = AppColors.LIGHT;
    private static final Color CLR_SUCCESS  = AppColors.SUCCESS;
    private static final Color CLR_DANGER   = AppColors.DANGER;
    private static final Color CLR_WARNING  = AppColors.WARNING;
    private static final Color CLR_ROW_ODD  = AppColors.WHITE;
    private static final Color CLR_ROW_EVEN = new Color(0xEEF7FA);
    private static final Color CLR_ROW_SEL  = new Color(0xB3E0F0);

    private static final Color CLR_CHART_BAR   = new Color(0x2196F3);
    private static final Color CLR_CHART_BAR2  = new Color(0xB3D8F7);
    private static final Color CLR_CHART_LINE  = new Color(0xFF6B35);
    private static final Color CLR_CHART_GRID  = new Color(0xE0E0E0);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_KPI_VALUE  = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_KPI_LABEL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_KPI_SUB    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_CHART_AXIS = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Period toggle ─────────────────────────────────────────────────────────
    private static final String[] PERIODS = { "7 ngày", "30 ngày", "12 tháng" };
    private int selectedPeriod = 0; // 0=7d, 1=30d, 2=12m

    // ── Components ────────────────────────────────────────────────────────────
    private ChartPanel         chartPanel;
    private JLabel             lblChartTitle;
    private JButton[]          periodBtns;
    private DefaultTableModel  mdlTopProducts;
    private DefaultTableModel  mdlLowStock;

    // ── Mock data ─────────────────────────────────────────────────────────────
    // TODO: Thay bằng dữ liệu thật từ NetworkService

    // Doanh thu 7 ngày gần nhất (đơn vị: nghìn đồng)
    private static final double[] DATA_7D  = { 8500, 12300, 9800, 15600, 11200, 18400, 14700 };
    private static final String[] LABEL_7D = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

    // Doanh thu 30 ngày (nhóm theo tuần, 4 tuần + hôm nay)
    private static final double[] DATA_30D  = { 62000, 75400, 68900, 91200, 14700 };
    private static final String[] LABEL_30D = { "Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Hôm nay" };

    // Doanh thu 12 tháng
    private static final double[] DATA_12M = {
            120000, 98000, 145000, 132000, 167000,
            154000, 178000, 162000, 189000, 201000, 175000, 220000
    };
    private static final String[] LABEL_12M = {
            "T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"
    };

    // KPI
    private static final long   KPI_TODAY        = 14_700_000L;
    private static final long   KPI_MONTH        = 220_000_000L;
    private static final int    KPI_ORDERS       = 47;
    private static final int    KPI_LOW_STOCK    = 5;

    // Sản phẩm bán chạy (Tên, Đã bán, Doanh thu)
    private static final Object[][] TOP_PRODUCTS = {
            { "Paracetamol 500mg",     1240, "24,800,000 đ" },
            { "Vitamin C 1000mg Syrup", 890, "44,500,000 đ" },
            { "Amoxicillin 500mg",      730, "36,500,000 đ" },
            { "Omeprazole 20mg",        610, "24,400,000 đ" },
            { "Omega 3 Fish Oil",       420, "42,000,000 đ" },
    };

    // Tồn kho thấp (Tên, Tồn, Cảnh báo)
    private static final Object[][] LOW_STOCK = {
            { "Amoxicillin 500mg",    23, "< 50" },
            { "Omeprazole 20mg",      41, "< 50" },
            { "Kẹo ngậm Strepsils",   15, "< 30" },
            { "Thuốc nhỏ mắt Rohto",   8, "< 20" },
            { "Metformin 500mg",      37, "< 50" },
    };

    // ══════════════════════════════════════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════════════════════════════════════
    public StatisticalGui(NetworkService networkService) {
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);
        initComponents();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI Init
    // ══════════════════════════════════════════════════════════════════════════
    private void initComponents() {
        add(buildTopBar(),  BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(CLR_BG);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        body.add(scroll, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(CLR_WHITE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, CLR_BORDER) {
            @Override public void paintBorder(Component c, Graphics g,
                                              int x, int y, int w, int h) {
                super.paintBorder(c, g, x, y, w, h);
            }
        });
        bar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, CLR_BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel lblTitle = new JLabel("Thống kê & Báo cáo");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(CLR_ACCENT);
        bar.add(lblTitle, BorderLayout.WEST);

        // Ngày hiện tại
        JLabel lblDate = new JLabel(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblDate.setFont(FONT_KPI_LABEL);
        lblDate.setForeground(CLR_MUTED);
        bar.add(lblDate, BorderLayout.EAST);

        return bar;
    }

    // ── Main body (scrollable) ─────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        body.add(buildKpiRow());
        body.add(Box.createVerticalStrut(16));
        body.add(buildChartCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildBottomTables());

        return body;
    }

    // ── KPI cards ─────────────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(CLR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        row.add(buildKpiCard("Doanh thu hôm nay",
                formatMoney(KPI_TODAY) + " đ",
                "↑ 12% so với hôm qua",
                CLR_PRIMARY, "💰"));

        row.add(buildKpiCard("Doanh thu tháng này",
                formatMoney(KPI_MONTH) + " đ",
                "↑ 8% so với tháng trước",
                CLR_SUCCESS, "📈"));

        row.add(buildKpiCard("Đơn hàng hôm nay",
                String.valueOf(KPI_ORDERS),
                "↑ 5 đơn so với hôm qua",
                new Color(0xFF9800), "🛒"));

        row.add(buildKpiCard("Tồn kho cần nhập",
                String.valueOf(KPI_LOW_STOCK) + " sản phẩm",
                "Dưới ngưỡng cảnh báo",
                CLR_DANGER, "⚠️"));

        return row;
    }

    private JPanel buildKpiCard(String label, String value, String sub,
                                Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CLR_WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Left accent bar
        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(4, 0));
        card.add(accent, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 2));
        content.setBackground(CLR_WHITE);
        content.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel lblLabel = new JLabel(icon + "  " + label);
        lblLabel.setFont(FONT_KPI_LABEL);
        lblLabel.setForeground(CLR_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FONT_KPI_VALUE);
        lblValue.setForeground(CLR_ACCENT);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(FONT_KPI_SUB);
        lblSub.setForeground(accentColor);

        content.add(lblLabel);
        content.add(lblValue);
        content.add(lblSub);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Chart card ────────────────────────────────────────────────────────────
    private JPanel buildChartCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CLR_WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Header: title + period toggle
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_WHITE);

        lblChartTitle = new JLabel("Doanh thu 7 ngày gần nhất");
        lblChartTitle.setFont(FONT_SECTION);
        lblChartTitle.setForeground(CLR_ACCENT);
        header.add(lblChartTitle, BorderLayout.WEST);

        // Period toggle buttons
        JPanel toggleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        toggleBar.setBackground(CLR_WHITE);
        periodBtns = new JButton[PERIODS.length];
        for (int i = 0; i < PERIODS.length; i++) {
            final int idx = i;
            JButton btn = new JButton(PERIODS[i]);
            btn.setFont(FONT_BTN);
            btn.setFocusPainted(false);
            btn.setBorderPainted(true);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(5, 12, 5, 12));
            btn.addActionListener(e -> setPeriod(idx));
            periodBtns[i] = btn;
            toggleBar.add(btn);
        }
        header.add(toggleBar, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Chart
        chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 240));
        card.add(chartPanel, BorderLayout.CENTER);

        // Init style
        setPeriod(0);

        return card;
    }

    private void setPeriod(int idx) {
        selectedPeriod = idx;
        for (int i = 0; i < periodBtns.length; i++) {
            boolean active = (i == idx);
            periodBtns[i].setBackground(active ? CLR_PRIMARY : CLR_WHITE);
            periodBtns[i].setForeground(active ? Color.WHITE : CLR_ACCENT);
        }
        switch (idx) {
            case 0 -> {
                lblChartTitle.setText("Doanh thu 7 ngày gần nhất");
                chartPanel.setData(DATA_7D, LABEL_7D, "nghìn đồng");
            }
            case 1 -> {
                lblChartTitle.setText("Doanh thu 30 ngày (theo tuần)");
                chartPanel.setData(DATA_30D, LABEL_30D, "nghìn đồng");
            }
            case 2 -> {
                lblChartTitle.setText("Doanh thu 12 tháng");
                chartPanel.setData(DATA_12M, LABEL_12M, "nghìn đồng");
            }
        }
    }

    // ── Bottom tables ─────────────────────────────────────────────────────────
    private JPanel buildBottomTables() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(CLR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        row.add(buildTableCard(
                "🏆 Sản phẩm bán chạy",
                new String[]{ "Tên sản phẩm", "Đã bán", "Doanh thu" },
                TOP_PRODUCTS,
                mdlTopProducts = new DefaultTableModel(
                        new String[]{"Tên sản phẩm","Đã bán","Doanh thu"}, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                }
        ));

        row.add(buildTableCard(
                "⚠️ Tồn kho thấp",
                new String[]{ "Tên sản phẩm", "Tồn kho", "Ngưỡng" },
                LOW_STOCK,
                mdlLowStock = new DefaultTableModel(
                        new String[]{"Tên sản phẩm","Tồn kho","Ngưỡng"}, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                }
        ));

        return row;
    }

    private JPanel buildTableCard(String title, String[] cols,
                                  Object[][] data, DefaultTableModel model) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CLR_WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(CLR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(CLR_ACCENT);
        lblTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lblTitle, BorderLayout.NORTH);

        // Load data vào model
        for (Object[] row : data) model.addRow(row);

        JTable table = new JTable(model);
        styleTable(table);

        // Cột số căn phải
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        if (table.getColumnCount() > 1) table.getColumnModel().getColumn(1).setCellRenderer(rightAlign);
        if (table.getColumnCount() > 2) table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(CLR_BORDER));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(CLR_ROW_SEL);
        table.setSelectionForeground(CLR_ACCENT);
        table.setFocusable(false);

        table.getTableHeader().setFont(FONT_TABLE_HDR);
        table.getTableHeader().setBackground(CLR_ACCENT);
        table.getTableHeader().setForeground(AppColors.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());

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
                    // Highlight tồn kho thấp (< 30) màu đỏ
                    if (val instanceof Integer v && v < 30 && col == 1) {
                        setForeground(CLR_DANGER);
                    } else {
                        setForeground(CLR_ACCENT);
                    }
                }
                return this;
            }
        });
    }

    // ── Utilities ─────────────────────────────────────────────────────────────
    private static String formatMoney(long val) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(val);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: ChartPanel (biểu đồ cột + đường trend, Java2D thuần)
    // ══════════════════════════════════════════════════════════════════════════
    private static class ChartPanel extends JPanel {

        private double[] data   = {};
        private String[] labels = {};
        private String   unit   = "";

        // Padding
        private static final int PAD_LEFT   = 72;
        private static final int PAD_RIGHT  = 20;
        private static final int PAD_TOP    = 20;
        private static final int PAD_BOTTOM = 36;

        ChartPanel() {
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        void setData(double[] data, String[] labels, String unit) {
            this.data   = data;
            this.labels = labels;
            this.unit   = unit;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int chartW = w - PAD_LEFT - PAD_RIGHT;
            int chartH = h - PAD_TOP  - PAD_BOTTOM;

            double maxVal = 0;
            for (double d : data) if (d > maxVal) maxVal = d;
            if (maxVal == 0) maxVal = 1;
            double niceMax = niceMax(maxVal);

            // ── Grid lines ──────────────────────────────────────────────────
            int gridLines = 5;
            g2.setFont(FONT_CHART_AXIS);
            g2.setColor(CLR_CHART_GRID);
            g2.setStroke(new BasicStroke(0.7f));

            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i <= gridLines; i++) {
                double val  = niceMax * i / gridLines;
                int    yPos = PAD_TOP + chartH - (int)(chartH * val / niceMax);

                g2.setColor(CLR_CHART_GRID);
                g2.drawLine(PAD_LEFT, yPos, PAD_LEFT + chartW, yPos);

                // Y axis label
                String label = formatAxisVal(val);
                g2.setColor(new Color(0x888888));
                g2.drawString(label, PAD_LEFT - fm.stringWidth(label) - 6,
                        yPos + fm.getAscent() / 2 - 1);
            }

            // ── Y axis line ─────────────────────────────────────────────────
            g2.setColor(CLR_CHART_GRID);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + chartH);

            // ── Bars ────────────────────────────────────────────────────────
            int n        = data.length;
            int slotW    = chartW / n;
            int barW     = Math.max(10, (int)(slotW * 0.55));
            int barOffX  = (slotW - barW) / 2;

            // Points for trend line
            int[] trendX = new int[n];
            int[] trendY = new int[n];

            for (int i = 0; i < n; i++) {
                int x     = PAD_LEFT + i * slotW + barOffX;
                int barH  = (int)(chartH * data[i] / niceMax);
                int y     = PAD_TOP + chartH - barH;

                // Bar gradient simulation (two rects)
                g2.setColor(CLR_CHART_BAR);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                // Subtle highlight on left side
                g2.setColor(CLR_CHART_BAR2.brighter());
                g2.fillRoundRect(x, y, barW / 3, barH, 4, 4);

                // Center of bar top for trend line
                trendX[i] = PAD_LEFT + i * slotW + slotW / 2;
                trendY[i] = y;

                // X axis label
                g2.setColor(new Color(0x555555));
                String lbl = (i < labels.length) ? labels[i] : "";
                int lblX   = PAD_LEFT + i * slotW + (slotW - fm.stringWidth(lbl)) / 2;
                g2.drawString(lbl, lblX, PAD_TOP + chartH + 16);

                // Value on top of bar (if bar is tall enough)
                if (barH > 22) {
                    String valStr = formatAxisVal(data[i]);
                    int vx = x + (barW - fm.stringWidth(valStr)) / 2;
                    g2.setColor(Color.WHITE);
                    g2.drawString(valStr, vx, y + 14);
                }
            }

            // ── Trend line ──────────────────────────────────────────────────
            g2.setColor(CLR_CHART_LINE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(trendX[i], trendY[i], trendX[i+1], trendY[i+1]);
            }

            // Dots on trend line
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.WHITE);
                g2.fillOval(trendX[i] - 4, trendY[i] - 4, 8, 8);
                g2.setColor(CLR_CHART_LINE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(trendX[i] - 4, trendY[i] - 4, 8, 8);
            }

            // ── Unit label (bottom-left) ─────────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g2.setColor(new Color(0xAAAAAA));
            g2.drawString("Đơn vị: " + unit, PAD_LEFT, h - 4);

            g2.dispose();
        }

        /** Làm tròn maxVal lên mức "đẹp" */
        private double niceMax(double val) {
            double exp   = Math.pow(10, Math.floor(Math.log10(val)));
            double frac  = val / exp;
            double nice  = frac <= 1 ? 1 : frac <= 2 ? 2 : frac <= 5 ? 5 : 10;
            return nice * exp * 1.1; // thêm 10% khoảng trống trên
        }

        private String formatAxisVal(double val) {
            if (val >= 1_000_000) return String.format("%.1fM", val / 1_000_000);
            if (val >= 1_000)     return String.format("%.0fK", val / 1_000);
            return String.format("%.0f", val);
        }
    }
}