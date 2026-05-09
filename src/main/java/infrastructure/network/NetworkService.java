package infrastructure.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * NetworkService đóng gói toàn bộ logic socket.
 * GUI chỉ gọi send() mà không cần biết về socket/stream.
 *
 * send() được synchronized để tránh race condition khi nhiều SwingWorker
 * (StaffGui, ProductGui, v.v.) gọi đồng thời trên cùng một stream.
 */
public class NetworkService {
    private final String host;
    private final int    port;

    private Socket             socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    public NetworkService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // ─── Kết nối đến server ──────────────────────────────────────────────────
    public void connect() throws Exception {
        socket = new Socket(host, port);
        // OOS trước, flush header, rồi mới tạo OIS — tránh "invalid type code: 00"
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in  = new ObjectInputStream(socket.getInputStream());
        System.out.println("Đã kết nối tới server " + host + ":" + port);
    }

    // ─── Gửi request và nhận response ────────────────────────────────────────
    // synchronized: ObjectInputStream/ObjectOutputStream KHÔNG thread-safe.
    // Nếu StaffGui và ProductGui load đồng thời, stream bị interleave →
    // "Index -1 out of bounds" hoặc "invalid type code: 00"
    public synchronized Response send(CommandType commandType, Object data) throws Exception {
        Request request = Request.builder()
                .commandType(commandType)
                .data(data)
                .build();
        out.writeObject(request);
        out.flush();
        out.reset(); // tránh cache object cũ trong stream
        return (Response) in.readObject();
    }

    // Overload không cần data
    public synchronized Response send(CommandType commandType) throws Exception {
        return send(commandType, null);
    }

    // ─── Đóng kết nối ────────────────────────────────────────────────────────
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
            System.out.println("Đã ngắt kết nối");
        } catch (Exception e) {
            System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}