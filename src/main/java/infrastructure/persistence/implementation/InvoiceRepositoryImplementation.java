package infrastructure.persistence.implementation;

import core.entities.*;
import core.entities.enums.LotStatus;
import infrastructure.persistence.InvoiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepositoryImplementation
        extends AbstractGenericRepositoryImplementation<Invoice, String>
        implements InvoiceRepository {

    public InvoiceRepositoryImplementation() {
        super(Invoice.class);
    }

    @Override
    public Invoice create(Invoice invoice) {
        return doInTransaction(em -> {
            invoice.setCreator(em.find(Staff.class, invoice.getCreator().getId()));
            invoice.setShift(em.find(Shift.class, invoice.getShift().getId()));

            if (invoice.getCustomer() != null) {
                String phone = invoice.getCustomer().getPhoneNumber();

                Customer existingCustomer = em.createQuery(
                                "FROM Customer c WHERE c.phoneNumber = :phone",
                                Customer.class
                        )
                        .setParameter("phone", phone)
                        .setMaxResults(1)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (existingCustomer != null) {
                    invoice.setCustomer(existingCustomer);
                } else {
                    invoice.getCustomer().setCreationDate(LocalDateTime.now());
                }
            }

            if (invoice.getPromotion() != null) {
                invoice.setPromotion(em.find(Promotion.class, invoice.getPromotion().getId()));
            }

            if (invoice.getReferencedInvoice() != null) {
                invoice.setReferencedInvoice(em.find(Invoice.class, invoice.getReferencedInvoice().getId()));
            }

            invoice.setInvoiceLines(invoice.getInvoiceLines().stream().map(invoiceLine -> {
                invoiceLine.setInvoice(invoice);

                UnitOfMeasure uom = em.find(
                        UnitOfMeasure.class,
                        UnitOfMeasure.UnitOfMeasureId.builder()
                                .product(invoiceLine.getUnitOfMeasure().getProduct().getId())
                                .measurement(invoiceLine.getUnitOfMeasure().getMeasurement().getId())
                                .build()
                );

                if (uom == null) {
                    throw new IllegalArgumentException("UnitOfMeasure not found");
                }

                invoiceLine.setUnitOfMeasure(uom);

                List<LotAllocation> freshAllocations = allocateLotsFreshAndLocked(
                        em,
                        invoiceLine,
                        uom.getProduct().getId()
                );

                invoiceLine.setLotAllocations(freshAllocations);
                return invoiceLine;
            }).toList());

            em.persist(invoice);
            return invoice;
        });
    }

    private List<LotAllocation> allocateLotsFreshAndLocked(
            EntityManager em,
            InvoiceLine invoiceLine,
            String productId
    ) {
        int remainingNeeded = convertToBaseQuantity(
                invoiceLine.getQuantity(),
                invoiceLine.getUnitOfMeasure()
        );

        List<Lot> lockedLots = em.createQuery("""
                        FROM Lot l
                        WHERE l.product.id = :productId
                          AND l.status = :status
                          AND l.quantity > 0
                          AND l.expiryDate > :now
                        ORDER BY l.expiryDate ASC, l.id ASC
                        """, Lot.class)
                .setParameter("productId", productId)
                .setParameter("status", LotStatus.AVAILABLE)
                .setParameter("now", LocalDateTime.now())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        List<LotAllocation> allocations = new ArrayList<>();

        for (Lot lot : lockedLots) {
            if (remainingNeeded <= 0) {
                break;
            }

            int take = Math.min(lot.getQuantity(), remainingNeeded);

            lot.setQuantity(lot.getQuantity() - take);

            LotAllocation allocation = LotAllocation.builder()
                    .invoiceLine(invoiceLine)
                    .lot(lot)
                    .quantity(take)
                    .build();

            allocations.add(allocation);
            remainingNeeded -= take;
        }

        if (remainingNeeded > 0) {
            throw new IllegalArgumentException(
                    "Không đủ tồn kho cho sản phẩm "
                            + productId
                            + ". Còn thiếu "
                            + remainingNeeded
                            + " đơn vị gốc."
            );
        }

        return allocations;
    }

    private int convertToBaseQuantity(int quantity, UnitOfMeasure uom) {
        if (uom == null || uom.isBaseUnit()) {
            return quantity;
        }

        BigDecimal rate = uom.getBaseUnitConversionRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return quantity;
        }

        return BigDecimal.valueOf(quantity)
                .multiply(rate)
                .setScale(0, RoundingMode.CEILING)
                .intValue();
    }
}