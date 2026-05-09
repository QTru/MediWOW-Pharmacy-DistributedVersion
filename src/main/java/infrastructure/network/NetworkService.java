package infrastructure.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * NetworkService đóng gói toàn bộ logic socket.
 * GUI chỉ gọi send() mà không cần biết về socket/stream.
 */
public class NetworkService {
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public NetworkService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // ─── Kết nối đến server ──────────────────────────────────────────────────
    public void connect() throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in  = new ObjectInputStream(socket.getInputStream());
        System.out.println("Đã kết nối tới server " + host + ": " + port);
    }

    // ─── Gửi request và nhận response ────────────────────────────────────────
    public Response send(CommandType commandType, Object data) throws Exception {
        Request request = Request.builder()
                .commandType(commandType)
                .data(data)
                .build();
        out.reset(); // prevent ObjectOutputStream from caching stale object references
        out.writeObject(request);
        out.flush();
        return (Response) in.readObject();
    }

    // Overload không cần data
    public Response send(CommandType commandType) throws Exception {
        return send(commandType, null);
    }

    // ─── Đóng kết nối ────────────────────────────────────────────────────────
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close(); // tự động đóng cả out và in
            System.out.println("Đã ngắt kết nối");
        } catch (Exception e) {
            System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}