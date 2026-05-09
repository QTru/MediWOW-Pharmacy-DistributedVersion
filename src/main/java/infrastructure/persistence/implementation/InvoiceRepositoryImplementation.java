package infrastructure.persistence.implementation;

import core.entities.*;
import core.entities.enums.InvoiceLineType;
import core.entities.enums.InvoiceType;
import core.entities.enums.PaymentMethod;
import infrastructure.persistence.InvoiceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceRepositoryImplementation extends AbstractGenericRepositoryImplementation<Invoice, String> implements InvoiceRepository {
    private final CustomerRepositoryImplementation customerRepository;

    public InvoiceRepositoryImplementation() {
        super(Invoice.class);
        customerRepository = new CustomerRepositoryImplementation();
    }

    @Override
    public Invoice create(Invoice invoice) {
        return doInTransaction(em -> {
            invoice.setCreator(em.find(Staff.class, invoice.getCreator().getId()));
            invoice.setShift(em.find(Shift.class, invoice.getShift().getId()));

            if (invoice.getCustomer() != null) {
                Customer customer = customerRepository.findByPhoneNumber(invoice.getCustomer().getPhoneNumber());
                if (customer != null)
                    invoice.setCustomer(em.merge(customer));
                else
                    invoice.getCustomer().setCreationDate(LocalDateTime.now());
            }

            invoice.setInvoiceLines(invoice.getInvoiceLines().stream().map(invoiceLine -> {
                invoiceLine.setInvoice(invoice);
                invoiceLine.setUnitOfMeasure(em.find(UnitOfMeasure.class,  // ← Change to find
                        UnitOfMeasure.UnitOfMeasureId.builder()
                                .product(invoiceLine.getUnitOfMeasure().getProduct().getId())
                                .measurement(invoiceLine.getUnitOfMeasure().getMeasurement().getId())
                                .build()));

                invoiceLine.setLotAllocations(invoiceLine.getLotAllocations().stream().map(lotAllocation -> {
                    lotAllocation.setInvoiceLine(invoiceLine);
                    Lot lot = em.find(Lot.class, lotAllocation.getLot().getId());
                    if (lot == null) {
                        throw new IllegalArgumentException("Lot not found: " + lotAllocation.getLot().getId());
                    }
                    if (lot.getQuantity() < lotAllocation.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient quantity in lot: " + lot.getId());
                    }
                    lot.setQuantity(lot.getQuantity() - lotAllocation.getQuantity());
                    lotAllocation.setLot(lot);
                    return lotAllocation;
                }).toList());

                return invoiceLine;
            }).toList());

            if (invoice.getPromotion() != null)
                invoice.setPromotion(em.find(Promotion.class, invoice.getPromotion().getId()));
            if (invoice.getReferencedInvoice() != null)
                invoice.setReferencedInvoice(em.find(Invoice.class, invoice.getReferencedInvoice().getId()));

            em.persist(invoice);
            return invoice;
        });
    }

    public static void main(String[] args) {
        InvoiceRepository invoiceRepository = new InvoiceRepositoryImplementation();

        Invoice invoice = Invoice
            .builder()
            .type(InvoiceType.RETURN)
            .creationDate(LocalDateTime.now())
            .creator(Staff.builder().id("STA0001").build())
            .shift(Shift.builder().id("SHI000001").build())
            .invoiceLines(List.of(
                InvoiceLine
                    .builder()
                    .type(InvoiceLineType.SALE)
                    .unitPrice(BigDecimal.valueOf(900))
                    .quantity(2)
                    .unitOfMeasure(UnitOfMeasure.builder()
                        .product(Product.builder().id("PRO000001").build())
                        .measurement(Measurement.builder().id("MEA0001").build())
                        .baseUnit(true)
                        .baseUnitConversionRate(BigDecimal.ONE)
                        .build())
                    .lotAllocations(List.of(
                        LotAllocation
                            .builder()
                            .lot(Lot.builder().id("LOT000001").build())
                            .quantity(2)
                            .build()
                    ))
                    .build()
            ))
            .paymentMethod(PaymentMethod.CASH_PAYMENT)
            .referencedInvoice(Invoice.builder().id("INV000002").build())
            .build();

        invoiceRepository.create(invoice);
    }
}
