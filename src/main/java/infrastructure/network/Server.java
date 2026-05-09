package infrastructure.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORT = 9090;
    private static final int THREAD_POOL_SIZE = 10;
    private static ServerSocket serverSocket;
    private static ExecutorService pool;

    public static void main(String[] args) {
        pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);
            System.out.println("Server đang chạy trên cổng " + PORT);

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(Server::shutdown));

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client kết nối: " + socket.getInetAddress());
                pool.submit(new ClientHandler(socket));
            }

        } catch (SocketException e) {
            if ("Socket is closed".equals(e.getMessage())) {
                System.out.println("Server đã dừng lại");
            } else {
                System.err.println("Server lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException("Server lỗi: " + e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    private static void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (pool != null) {
                pool.shutdown();
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            }
            System.out.println("Server đã tắt");
        } catch (Exception e) {
            System.err.println("Lỗi khi tắt server: " + e.getMessage());
        }
    }
}
