package infrastructure.network;

import core.dto.StaffDto;
import infrastructure.service.StaffService;
import infrastructure.service.implementation.StaffServiceImplementation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final StaffService staffService = new StaffServiceImplementation();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in  = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                // 1. Nhận Request từ client
                Request request = (Request) in.readObject();
                System.out.println("[Server] Nhận: " + request.getCommandType());

                // 2. Xử lý và tạo Response
                Response response = handle(request);

                // 3. Gửi Response về client
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client ngắt kết nối: " + socket.getInetAddress());
        }
    }

    private Response handle(Request request) {
        return switch (request.getCommandType()) {
            case PING -> Response.builder()
                    .success(true)
                    .data("PONG")
                    .message("Server phản hồi thành công!")
                    .build();

            // ─── Staff ────────────────────────────────────────────────────────
            case STAFF_LOGIN -> handleStaffLogin(request);
            case STAFF_CREATE -> null;
            case STAFF_UPDATE -> null;
            case STAFF_FIND_BY_ID -> null;
            case STAFF_LOAD_ALL -> null;
            case PRODUCT_CREATE -> null;
            case PRODUCT_UPDATE -> null;
            case PRODUCT_FIND_BY_ID -> null;
            case PRODUCT_LOAD_ALL -> null;
            case INVOICE_CREATE -> null;
            case INVOICE_FIND_BY_ID -> null;
            case INVOICE_LOAD_ALL -> null;
            case SHIFT_CREATE -> null;
            case SHIFT_UPDATE -> null;
            case SHIFT_FIND_BY_ID -> null;
            case SHIFT_GET_ACTIVE -> null;
            case LOT_CREATE -> null;
            case LOT_UPDATE -> null;
            case LOT_FIND_BY_ID -> null;
            case LOT_LOAD_ALL -> null;
            case CUSTOMER_CREATE -> null;
            case CUSTOMER_UPDATE -> null;
            case CUSTOMER_FIND_BY_ID -> null;
            case CUSTOMER_FIND_BY_PHONE -> null;
            case PROMOTION_CREATE -> null;
            case PROMOTION_UPDATE -> null;
            case PROMOTION_FIND_BY_ID -> null;
            case PROMOTION_LOAD_ALL -> null;
            case MEASUREMENT_FIND_BY_ID -> null;
            case MEASUREMENT_LOAD_ALL -> null;
        };
    }

    // ─── Staff Login Handler ──────────────────────────────────────────────────
    private Response handleStaffLogin(Request request) {
        try {
            String[] credentials = (String[]) request.getData();
            String username = credentials[0];
            String password = credentials[1];
            StaffDto staffDto = staffService.login(username, password);
            return Response.builder()
                    .success(true)
                    .data(staffDto)
                    .message("Đăng nhập thành công! Xin chào, " + staffDto.getFullName())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.builder()
                    .success(false)
                    .message("Lỗi hệ thống. Vui lòng thử lại sau.")
                    .build();
        }
    }
}
