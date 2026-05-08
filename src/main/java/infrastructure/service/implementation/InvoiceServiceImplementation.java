package infrastructure.service.implementation;

import core.dto.InvoiceDto;
import core.dto.InvoiceLineDto;
import core.dto.LotAllocationDto;
import core.entities.Invoice;
import core.entities.enums.InvoiceLineType;
import core.entities.enums.InvoiceType;
import core.entities.enums.PaymentMethod;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.InvoiceRepository;
import infrastructure.persistence.implementation.InvoiceRepositoryImplementation;
import infrastructure.service.InvoiceService;

import java.math.BigDecimal;
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
        System.err.println("Invoice update is not allowed");

        return null;
    }

    @Override
    public InvoiceDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Invoice invoice = invoiceRepository.findById(id);
        if (invoice == null)
            throw new IllegalArgumentException("Invoice with id " + id + " not found");

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
        return phoneNumber != null && !phoneNumber.isBlank() && phoneNumber.chars().allMatch(Character::isDigit);
    }

    private void checkGeneralInfo(InvoiceDto invoiceDto) {
        if (invoiceDto.getType() == null)
            throw new IllegalArgumentException("Invoice type cannot be null");
        if (invoiceDto.getPaymentMethod() == null)
            throw new IllegalArgumentException("Payment method cannot be null");
        if (invoiceDto.getCreatorId() == null || invoiceDto.getCreatorId().isBlank())
            throw new IllegalArgumentException("Creator id cannot be null or blank");
        if (invoiceDto.getShiftId() == null || invoiceDto.getShiftId().isBlank())
            throw new IllegalArgumentException("Shift id cannot be null or blank");
        if (!isPhoneNumberValid(invoiceDto.getCustomerPhoneNumber()))
            throw new IllegalArgumentException("Customer phone number must be a non-empty string of digits");
        if (invoiceDto.getInvoiceLines() == null || invoiceDto.getInvoiceLines().isEmpty())
            throw new IllegalArgumentException("Invoice must have at least one invoice line");
        invoiceDto.getInvoiceLines().forEach(invoiceLineDto -> {
            if (invoiceLineDto.getProductId() == null || invoiceLineDto.getProductId().isBlank())
                throw new IllegalArgumentException("Invoice line must have a product id");
            if (invoiceLineDto.getMeasurementId() == null || invoiceLineDto.getMeasurementId().isBlank())
                throw new IllegalArgumentException("Invoice line must have a measurement id");
            if (invoiceLineDto.getType() == null)
                throw new IllegalArgumentException("Invoice line must have a type");
            if ((invoiceDto.getType() == InvoiceType.RETURN && invoiceLineDto.getType() != InvoiceLineType.RETURN)
                    || (invoiceDto.getType() == InvoiceType.SALE && invoiceLineDto.getType() != InvoiceLineType.SALE && invoiceLineDto.getType() != InvoiceLineType.GIFT)
                    || (invoiceDto.getType() == InvoiceType.EXCHANGE && invoiceLineDto.getType() != InvoiceLineType.EXCHANGE_IN && invoiceLineDto.getType() != InvoiceLineType.EXCHANGE_OUT))
                throw new IllegalArgumentException("Invoice line type " + invoiceLineDto.getType() + " is not compatible with invoice type " + invoiceDto.getType());
            if (invoiceLineDto.getUnitPrice() == null || invoiceLineDto.getUnitPrice().signum() < 0)
                throw new IllegalArgumentException("Invoice line must have a non-negative unit price");
            if (invoiceLineDto.getQuantity() <= 0)
                throw new IllegalArgumentException("Invoice line must have a positive quantity");
            if (invoiceLineDto.getLotAllocations() == null || invoiceLineDto.getLotAllocations().isEmpty())
                throw new IllegalArgumentException("Invoice line must have at least one lot allocation");
            invoiceLineDto.getLotAllocations().forEach(lotAllocationDto -> {
                if (lotAllocationDto.getLotId() == null || lotAllocationDto.getLotId().isBlank())
                    throw new IllegalArgumentException("Lot allocation must have a lot id");
                if (lotAllocationDto.getQuantity() <= 0)
                    throw new IllegalArgumentException("Lot allocation must have a positive quantity");
            });
        });
    }

    public static void main(String[] args) {
        InvoiceService invoiceService = new InvoiceServiceImplementation();

        InvoiceDto invoiceDto = InvoiceDto
            .builder()
            .type(InvoiceType.SALE)
            .paymentMethod(PaymentMethod.CASH_PAYMENT)
            .creatorId("STA0001")
            .shiftId("SHI000001")
            .customerPhoneNumber("1234567892")
            .invoiceLines(List.of(
                InvoiceLineDto
                    .builder()
                    .productId("PRO000001")
                    .measurementId("MEA0003")
                    .unitPrice(BigDecimal.valueOf(900))
                    .type(InvoiceLineType.SALE)
                    .quantity(2)
                    .lotAllocations(List.of(
                        LotAllocationDto
                            .builder()
                            .lotId("LOT000001")
                            .quantity(2)
                            .build()
                    ))
                    .build()
            ))
            .build();

        InvoiceDto createdInvoice = invoiceService.create(invoiceDto);
        System.out.println(createdInvoice);
    }
}
