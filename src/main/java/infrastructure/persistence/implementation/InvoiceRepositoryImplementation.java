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
            if (invoice.getCreator() == null)
                throw new IllegalArgumentException("Invoice must have a creator");
            invoice.setCreator(em.getReference(Staff.class, invoice.getCreator().getId()));

            if (invoice.getShift() == null)
                throw new IllegalArgumentException("Invoice must have a shift");
            invoice.setShift(em.getReference(Shift.class, invoice.getShift().getId()));

            if (invoice.getCustomer() != null) {
                Customer customer = (Customer) customerRepository.findByPhoneNumber(invoice.getCustomer().getPhoneNumber());
                if (customer != null)
                    invoice.setCustomer(em.merge(customer)); // Assure managed state to prevent duplicate entry
            }

            if (invoice.getInvoiceLines() == null || invoice.getInvoiceLines().isEmpty())
                throw new IllegalArgumentException("Invoice must have at least one invoice line");
            invoice.setInvoiceLines(invoice.getInvoiceLines().stream().map(invoiceLine -> {
                invoiceLine.setInvoice(invoice);

                if (invoiceLine.getUnitOfMeasure() == null)
                    throw new IllegalArgumentException("Invoice line must have a unit of measure");
                if (invoiceLine.getUnitOfMeasure().getProduct() == null)
                    throw new IllegalArgumentException("Invoice line's unit of measure must have a product");
                if (invoiceLine.getUnitOfMeasure().getMeasurement() == null)
                    throw new IllegalArgumentException("Invoice line's unit of measure must have a measurement");
                invoiceLine.setUnitOfMeasure(em.getReference(UnitOfMeasure.class, UnitOfMeasure.UnitOfMeasureId
                        .builder()
                        .product(invoiceLine.getUnitOfMeasure().getProduct().getId())
                        .measurement(invoiceLine.getUnitOfMeasure().getMeasurement().getId())
                        .build()
                ));

                if (invoiceLine.getType() == null)
                    throw new IllegalArgumentException("Invoice line must have a type");

                if (invoiceLine.getLotAllocations() == null || invoiceLine.getLotAllocations().isEmpty())
                    throw new IllegalArgumentException("Invoice line must have at least one lot allocation");
                invoiceLine.setLotAllocations(invoiceLine.getLotAllocations().stream().map(lotAllocation -> {
                    lotAllocation.setInvoiceLine(invoiceLine);
                    if (lotAllocation.getLot() == null)
                        throw new IllegalArgumentException("Lot allocation must have a lot");
                    lotAllocation.setLot(em.getReference(Lot.class, lotAllocation.getLot().getId()));

                    return lotAllocation;
                }).toList());

                return invoiceLine;
            }).toList());

            if (invoice.getPromotion() != null)
                invoice.setPromotion(em.getReference(Promotion.class, invoice.getPromotion().getId()));

            if (invoice.getParentInvoice() != null)
                invoice.setParentInvoice(em.getReference(Invoice.class, invoice.getParentInvoice().getId()));

            if (invoice.getReferencedInvoice() != null)
                invoice.setReferencedInvoice(em.getReference(Invoice.class, invoice.getReferencedInvoice().getId()));

            em.persist(invoice);
            return invoice;
        });
    }

    public static void main(String[] args) {
            InvoiceRepository invoiceRepository = new InvoiceRepositoryImplementation();

            Invoice invoice = Invoice
                    .builder()
                    .type(InvoiceType.SALE)
                    .creationDate(LocalDateTime.now())
                    .creator(Staff.builder().id("STA0001").build())
                    .shift(Shift.builder().id("SHI000001").build())
                    .customer(Customer.builder().phoneNumber("1234567890").build())
                    .invoiceLines(List.of(
                            InvoiceLine
                                    .builder()
                                    .type(InvoiceLineType.SALE)
                                    .unitPrice(BigDecimal.valueOf(900))
                                    .quantity(2)
                                    .unitOfMeasure(UnitOfMeasure.builder()
                                            .product(Product.builder().id("PRO000001").build())
                                            .measurement(Measurement.builder().id("MEA0001").build())
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
                    .build();

            invoiceRepository.create(invoice);
    }
}
