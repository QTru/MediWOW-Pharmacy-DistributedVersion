package infrastructure.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9090;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy trên cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client kết nối: " + socket.getInetAddress());
                pool.submit(new ClientHandler(socket));
            }

        } catch (Exception e) {
            throw new RuntimeException("Server lỗi: " + e.getMessage(), e);
        }
    }
}
