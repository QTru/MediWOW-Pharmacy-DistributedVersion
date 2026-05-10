package infrastructure.network;

import presentation.LoginGui;

import javax.swing.*;

/**
 * Client — Entry point của ứng dụng client.
 * Khởi tạo NetworkService, kết nối server, sau đó mở LoginGui.
 * GUI không biết gì về socket, chỉ biết NetworkService.
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int    PORT = 9090;

    public static void main(String[] args) {
        NetworkService networkService = new NetworkService(HOST, PORT);

        try {
            // 1. Kết nối tới server trước khi mở GUI
            networkService.connect();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối tới server!\n" + e.getMessage(),
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE
            );
            return; // Dừng nếu không kết nối được
        }

        // 2. Mở LoginGui trên Event Dispatch Thread
        // — Swing không thread-safe, phải chạy trên EDT
        SwingUtilities.invokeLater(() -> {
            LoginGui loginGui = new LoginGui(networkService);
            loginGui.setVisible(true);
        });
    }
}