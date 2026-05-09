package infrastructure.network;

import core.dto.LotDto;
import core.dto.MeasurementDto;
import core.dto.ProductDto;
import core.dto.StaffDto;
import infrastructure.service.LotService;
import infrastructure.service.MeasurementService;
import infrastructure.service.ProductService;
import infrastructure.service.StaffService;
import infrastructure.service.implementation.LotServiceImplementation;
import infrastructure.service.implementation.MeasurementServiceImplementation;
import infrastructure.service.implementation.ProductServiceImplementation;
import infrastructure.service.implementation.StaffServiceImplementation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    private final StaffService       staffService       = new StaffServiceImplementation();
    private final ProductService     productService     = new ProductServiceImplementation();
    private final LotService         lotService         = new LotServiceImplementation();
    private final MeasurementService measurementService = new MeasurementServiceImplementation();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Request request = (Request) in.readObject();
                System.out.println("[Server] Nhận: " + request.getCommandType());

                Response response = handle(request);

                out.writeObject(response);
                out.flush();
                out.reset(); // ← quan trọng: tránh cache object cũ trong stream
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

            // ── Staff ─────────────────────────────────────────────────────────
            case STAFF_LOGIN      -> handleStaffLogin(request);
            case STAFF_CREATE     -> notImplemented();
            case STAFF_UPDATE     -> notImplemented();
            case STAFF_FIND_BY_ID -> notImplemented();
            case STAFF_LOAD_ALL   -> notImplemented();

            // ── Product ───────────────────────────────────────────────────────
            case PRODUCT_CREATE     -> handleProductCreate(request);
            case PRODUCT_UPDATE     -> handleProductUpdate(request);
            case PRODUCT_FIND_BY_ID -> handleProductFindById(request);
            case PRODUCT_LOAD_ALL   -> handleProductLoadAll();

            // ── Lot ───────────────────────────────────────────────────────────
            case LOT_CREATE     -> handleLotCreate(request);
            case LOT_UPDATE     -> handleLotUpdate(request);
            case LOT_FIND_BY_ID -> handleLotFindById(request);
            case LOT_LOAD_ALL   -> handleLotLoadAll();

            // ── Measurement ───────────────────────────────────────────────────
            case MEASUREMENT_FIND_BY_ID -> handleMeasurementFindById(request);
            case MEASUREMENT_LOAD_ALL   -> handleMeasurementLoadAll();

            // ── Chưa implement ────────────────────────────────────────────────
            case INVOICE_CREATE, INVOICE_FIND_BY_ID, INVOICE_LOAD_ALL,
                 SHIFT_CREATE, SHIFT_UPDATE, SHIFT_FIND_BY_ID, SHIFT_GET_ACTIVE,
                 CUSTOMER_CREATE, CUSTOMER_UPDATE, CUSTOMER_FIND_BY_ID, CUSTOMER_FIND_BY_PHONE,
                 PROMOTION_CREATE, PROMOTION_UPDATE, PROMOTION_FIND_BY_ID, PROMOTION_LOAD_ALL
                    -> notImplemented();
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Staff
    // ═══════════════════════════════════════════════════════════════════════════
    private Response handleStaffLogin(Request request) {
        try {
            String[] credentials = (String[]) request.getData();
            StaffDto staffDto = staffService.login(credentials[0], credentials[1]);
            return Response.builder()
                    .success(true)
                    .data(staffDto)
                    .message("Đăng nhập thành công! Xin chào, " + staffDto.getFullName())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Product
    // ═══════════════════════════════════════════════════════════════════════════
    private Response handleProductLoadAll() {
        try {
            return Response.builder()
                    .success(true)
                    .data(productService.loadAll())
                    .message("Tải danh sách sản phẩm thành công.")
                    .build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleProductFindById(Request request) {
        try {
            String id = (String) request.getData();
            return Response.builder()
                    .success(true)
                    .data(productService.findById(id))
                    .message("Tìm sản phẩm thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleProductCreate(Request request) {
        try {
            ProductDto dto = (ProductDto) request.getData();
            return Response.builder()
                    .success(true)
                    .data(productService.create(dto))
                    .message("Tạo sản phẩm thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleProductUpdate(Request request) {
        try {
            ProductDto dto = (ProductDto) request.getData();
            return Response.builder()
                    .success(true)
                    .data(productService.update(dto))
                    .message("Cập nhật sản phẩm thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Lot
    // ═══════════════════════════════════════════════════════════════════════════
    private Response handleLotLoadAll() {
        try {
            return Response.builder()
                    .success(true)
                    .data(lotService.loadAll())
                    .message("Tải danh sách lô hàng thành công.")
                    .build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleLotFindById(Request request) {
        try {
            String id = (String) request.getData();
            return Response.builder()
                    .success(true)
                    .data(lotService.findById(id))
                    .message("Tìm lô hàng thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleLotCreate(Request request) {
        try {
            LotDto dto = (LotDto) request.getData();
            return Response.builder()
                    .success(true)
                    .data(lotService.create(dto))
                    .message("Tạo lô hàng thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleLotUpdate(Request request) {
        try {
            LotDto dto = (LotDto) request.getData();
            return Response.builder()
                    .success(true)
                    .data(lotService.update(dto))
                    .message("Cập nhật lô hàng thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Measurement
    // ═══════════════════════════════════════════════════════════════════════════
    private Response handleMeasurementLoadAll() {
        try {
            return Response.builder()
                    .success(true)
                    .data(measurementService.loadAll())
                    .message("Tải danh sách đơn vị đo lường thành công.")
                    .build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response handleMeasurementFindById(Request request) {
        try {
            String id = (String) request.getData();
            return Response.builder()
                    .success(true)
                    .data(measurementService.findById(id))
                    .message("Tìm đơn vị đo lường thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════
    private Response notImplemented() {
        return Response.builder()
                .success(false)
                .message("Chức năng chưa được implement.")
                .build();
    }

    private Response errorResponse(Exception e) {
        System.err.println("[Server] Lỗi xử lý: " + e.getMessage());
        e.printStackTrace();
        return Response.builder()
                .success(false)
                .message("Lỗi hệ thống: " + e.getMessage())
                .build();
    }
}