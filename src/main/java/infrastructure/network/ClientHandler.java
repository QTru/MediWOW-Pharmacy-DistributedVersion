package infrastructure.network;

import core.dto.LotDto;
import core.dto.MeasurementDto;
import core.dto.ProductDto;
import core.dto.StaffDto;
import core.dto.CustomerDto;
import core.dto.InvoiceDto;
import core.dto.PromotionDto;
import core.dto.ShiftDto;
import infrastructure.service.CustomerService;
import infrastructure.service.InvoiceService;
import infrastructure.service.LotService;
import infrastructure.service.MeasurementService;
import infrastructure.service.ProductService;
import infrastructure.service.PromotionService;
import infrastructure.service.StaffService;
import infrastructure.service.ShiftService;
import infrastructure.service.implementation.CustomerServiceImplementation;
import infrastructure.service.implementation.InvoiceServiceImplementation;
import infrastructure.service.implementation.LotServiceImplementation;
import infrastructure.service.implementation.MeasurementServiceImplementation;
import infrastructure.service.implementation.ProductServiceImplementation;
import infrastructure.service.implementation.PromotionServiceImplementation;
import infrastructure.service.implementation.StaffServiceImplementation;
import infrastructure.service.implementation.ShiftServiceImplementation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    private final StaffService       staffService       = new StaffServiceImplementation();
    private final ProductService     productService     = new ProductServiceImplementation();
    private final LotService         lotService         = new LotServiceImplementation();
    private final MeasurementService measurementService = new MeasurementServiceImplementation();
    private final InvoiceService     invoiceService     = new InvoiceServiceImplementation();
    private final ShiftService       shiftService       = new ShiftServiceImplementation();
    private final CustomerService    customerService    = new CustomerServiceImplementation();
    private final PromotionService   promotionService   = new PromotionServiceImplementation();

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
            case INVOICE_CREATE     -> handleInvoiceCreate(request);
            case INVOICE_FIND_BY_ID -> notImplemented();
            case INVOICE_LOAD_ALL   -> notImplemented();

            case SHIFT_CREATE     -> notImplemented();
            case SHIFT_UPDATE     -> notImplemented();
            case SHIFT_FIND_BY_ID -> notImplemented();
            case SHIFT_GET_ACTIVE -> handleShiftGetActive(request);

            case CUSTOMER_CREATE        -> notImplemented();
            case CUSTOMER_UPDATE        -> notImplemented();
            case CUSTOMER_FIND_BY_ID    -> notImplemented();
            case CUSTOMER_FIND_BY_PHONE -> handleCustomerFindByPhone(request);

            case PROMOTION_CREATE     -> notImplemented();
            case PROMOTION_UPDATE     -> notImplemented();
            case PROMOTION_FIND_BY_ID -> notImplemented();
            case PROMOTION_LOAD_ALL   -> handlePromotionLoadAll();
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

    // ═════════════════════════════════════════════════════════════════════════════════════
    // Invoice
    // ═════════════════════════════════════════════════════════════════════════════════════
    private Response handleInvoiceCreate(Request request) {
        try {
            InvoiceDto dto = (InvoiceDto) request.getData();
            return Response.builder()
                    .success(true)
                    .data(invoiceService.create(dto))
                    .message("Tạo hóa đơn thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════════════
    // Shift
    // ═════════════════════════════════════════════════════════════════════════════════════
    private Response handleShiftGetActive(Request request) {
        try {
            String workStation = (String) request.getData();
            ShiftDto shiftDto = shiftService.findActiveByWorkStation(workStation);
            return Response.builder()
                    .success(true)
                    .data(shiftDto)
                    .message(shiftDto == null
                            ? "Không có ca làm việc đang mở trên máy này."
                            : "Lấy ca làm việc hiện tại thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════════════
    // Customer
    // ═════════════════════════════════════════════════════════════════════════════════════
    private Response handleCustomerFindByPhone(Request request) {
        try {
            String phoneNumber = (String) request.getData();
            CustomerDto customerDto = customerService.findByPhoneNumber(phoneNumber);
            return Response.builder()
                    .success(true)
                    .data(customerDto)
                    .message("Tìm khách hàng theo số điện thoại thành công.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder().success(false).message(e.getMessage()).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════════════
    // Promotion
    // ═════════════════════════════════════════════════════════════════════════════════════
    private Response handlePromotionLoadAll() {
        try {
            return Response.builder()
                    .success(true)
                    .data(promotionService.loadAll())
                    .message("Tải danh sách khuyến mãi thành công.")
                    .build();
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
