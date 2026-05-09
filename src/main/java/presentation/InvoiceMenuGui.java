package presentation;

import core.dto.StaffDto;
import infrastructure.network.NetworkService;
import presentation.subinvoices.SalesInvoiceGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InvoiceMenuGui extends JPanel implements ActionListener {
    private static final Color CLR_BG = AppColors.BACKGROUND;
    private static final Color CLR_NAV = AppColors.DARK;
    private static final Color CLR_ACTIVE = AppColors.WHITE;
    private static final Color CLR_TEXT = AppColors.TEXT;
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JButton btnSalesInvoice;
    private final JButton btnExchangeInvoice;
    private final JButton btnReturnInvoice;
    private final JButton btnInvoiceList;

    private JButton activeButton;

    public InvoiceMenuGui(NetworkService networkService, StaffDto currentStaff) {
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);

        btnSalesInvoice = createNavButton("Hóa đơn mua");
        btnExchangeInvoice = createNavButton("Hóa đơn đổi");
        btnReturnInvoice = createNavButton("Hóa đơn trả");
        btnInvoiceList = createNavButton("Danh sách hóa đơn");

        add(buildNavBar(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(new SalesInvoiceGui(networkService, currentStaff), "sales");
        // TODO: Replace placeholder cards when exchange, return, and invoice list modules are implemented.
        contentPanel.add(buildPlaceholder("Hóa đơn đổi"), "exchange");
        contentPanel.add(buildPlaceholder("Hóa đơn trả"), "return");
        contentPanel.add(buildPlaceholder("Danh sách hóa đơn"), "list");

        setActiveButton(btnSalesInvoice);
        cardLayout.show(contentPanel, "sales");
    }

    private JPanel buildNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(CLR_NAV);
        navBar.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        eastPanel.setOpaque(false);
        eastPanel.add(btnSalesInvoice);
        eastPanel.add(btnExchangeInvoice);
        eastPanel.add(btnReturnInvoice);
        eastPanel.add(btnInvoiceList);

        navBar.add(eastPanel, BorderLayout.EAST);
        return navBar;
    }

    private JPanel buildPlaceholder(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.WHITE);
        JLabel label = new JLabel(title + " đang được phát triển.");
        label.setForeground(AppColors.PLACEHOLDER_TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        panel.add(label);
        return panel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(CLR_TEXT);
        button.setBackground(CLR_NAV);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(this);
        return button;
    }

    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            activeButton.setBackground(CLR_NAV);
        }
        activeButton = button;
        activeButton.setBackground(CLR_ACTIVE);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnSalesInvoice) {
            setActiveButton(btnSalesInvoice);
            cardLayout.show(contentPanel, "sales");
        } else if (source == btnExchangeInvoice) {
            setActiveButton(btnExchangeInvoice);
            cardLayout.show(contentPanel, "exchange");
        } else if (source == btnReturnInvoice) {
            setActiveButton(btnReturnInvoice);
            cardLayout.show(contentPanel, "return");
        } else if (source == btnInvoiceList) {
            setActiveButton(btnInvoiceList);
            cardLayout.show(contentPanel, "list");
        }
    }
}
