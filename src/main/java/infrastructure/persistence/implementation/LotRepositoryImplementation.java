package infrastructure.persistence.implementation;

import core.entities.Lot;
import core.entities.enums.LotStatus;
import infrastructure.persistence.LotRepository;

import java.time.LocalDateTime;
import java.util.List;

public class LotRepositoryImplementation
        extends AbstractGenericRepositoryImplementation<Lot, String>
        implements LotRepository {

    public LotRepositoryImplementation() {
        super(Lot.class);
    }

    @Override
    public List<Lot> findAvailableLotsByProductId(String productId) {
        return doInTransaction(em -> em.createQuery("""
                        FROM Lot l
                        WHERE l.product.id = :productId
                          AND l.status = :status
                          AND l.quantity > 0
                          AND l.expiryDate > :now
                        ORDER BY l.expiryDate ASC
                        """, Lot.class)
                .setParameter("productId", productId)
                .setParameter("status", LotStatus.AVAILABLE)
                .setParameter("now", LocalDateTime.now())
                .getResultList());
    }
}