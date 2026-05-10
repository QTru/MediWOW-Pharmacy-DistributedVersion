package infrastructure.service.implementation;

import core.dto.InvoiceDto;
import core.dto.InvoiceLineDto;
import core.dto.LotAllocationDto;
import core.entities.Invoice;
import core.entities.enums.InvoiceLineType;
import core.entities.enums.InvoiceType;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.InvoiceRepository;
import infrastructure.persistence.implementation.InvoiceRepositoryImplementation;
import infrastructure.service.InvoiceService;

import java.time.LocalDateTime;
import java.util.List;

public class InvoiceServiceImplementation implements InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImplementation() {
        this.invoiceRepository = new InvoiceRepositoryImplementation();
    }

    @Override
    public InvoiceDto create(InvoiceDto invoiceDto) {
        checkGeneralInfo(invoiceDto);

        Invoice invoice = Mapper.map(invoiceDto);
        invoice.setCreationDate(LocalDateTime.now());

        invoice = invoiceRepository.create(invoice);
        return Mapper.map(invoice);
    }

    @Override
    public InvoiceDto update(InvoiceDto invoiceDto) {
        throw new UnsupportedOperationException("Invoice update is not allowed");
    }

    @Override
    public InvoiceDto findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }

        Invoice invoice = invoiceRepository.findById(id);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice with id " + id + " not found");
        }

        return Mapper.map(invoice);
    }

    @Override
    public List<InvoiceDto> loadAll() {
        return invoiceRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .toList();
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null
                && !phoneNumber.isBlank()
                && phoneNumber.chars().allMatch(Character::isDigit);
    }

    private void checkGeneralInfo(InvoiceDto invoiceDto) {
        if (invoiceDto == null) {
            throw new IllegalArgumentException("Invoice cannot be null");
        }
        if (invoiceDto.getType() == null) {
            throw new IllegalArgumentException("Invoice type cannot be null");
        }
        if (invoiceDto.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        if (invoiceDto.getCreatorId() == null || invoiceDto.getCreatorId().isBlank()) {
            throw new IllegalArgumentException("Creator id cannot be null or blank");
        }
        if (invoiceDto.getShiftId() == null || invoiceDto.getShiftId().isBlank()) {
            throw new IllegalArgumentException("Shift id cannot be null or blank");
        }
        if (invoiceDto.getCustomerPhoneNumber() != null
                && !invoiceDto.getCustomerPhoneNumber().isBlank()
                && !isPhoneNumberValid(invoiceDto.getCustomerPhoneNumber())) {
            throw new IllegalArgumentException("Customer phone number must be digits only");
        }
        if (invoiceDto.getInvoiceLines() == null || invoiceDto.getInvoiceLines().isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one invoice line");
        }

        for (InvoiceLineDto line : invoiceDto.getInvoiceLines()) {
            checkInvoiceLine(invoiceDto, line);
        }
    }

    private void checkInvoiceLine(InvoiceDto invoiceDto, InvoiceLineDto line) {
        if (line.getProductId() == null || line.getProductId().isBlank()) {
            throw new IllegalArgumentException("Invoice line must have product id");
        }
        if (line.getMeasurementId() == null || line.getMeasurementId().isBlank()) {
            throw new IllegalArgumentException("Invoice line must have measurement id");
        }
        if (line.getType() == null) {
            throw new IllegalArgumentException("Invoice line must have type");
        }

        boolean compatible =
                invoiceDto.getType() == InvoiceType.SALE
                        && (line.getType() == InvoiceLineType.SALE || line.getType() == InvoiceLineType.GIFT)
                        || invoiceDto.getType() == InvoiceType.RETURN
                        && line.getType() == InvoiceLineType.RETURN
                        || invoiceDto.getType() == InvoiceType.EXCHANGE
                        && (line.getType() == InvoiceLineType.EXCHANGE_IN
                        || line.getType() == InvoiceLineType.EXCHANGE_OUT);

        if (!compatible) {
            throw new IllegalArgumentException(
                    "Invoice line type " + line.getType()
                            + " is not compatible with invoice type " + invoiceDto.getType()
            );
        }

        if (line.getUnitPrice() == null || line.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("Invoice line must have non-negative unit price");
        }
        if (line.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invoice line quantity must be positive");
        }

        // IMPORTANT:
        // Do NOT require lotAllocations from client.
        // Client data may be stale when 2 clients are open.
        // Server will allocate fresh locked lots inside InvoiceRepositoryImplementation.create().
        if (line.getLotAllocations() != null) {
            for (LotAllocationDto allocation : line.getLotAllocations()) {
                if (allocation.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Lot allocation quantity must be positive");
                }
            }
        }
    }
}