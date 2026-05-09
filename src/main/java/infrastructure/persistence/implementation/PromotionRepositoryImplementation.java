package infrastructure.persistence.implementation;

import core.entities.*;
import infrastructure.persistence.PromotionRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class PromotionRepositoryImplementation
        extends AbstractGenericRepositoryImplementation<Promotion, String>
        implements PromotionRepository {

    public PromotionRepositoryImplementation() {
        super(Promotion.class);
    }

    @Override
    public Promotion create(Promotion promotion) {
        return doInTransaction(em -> {
            promotion.setCreationDate(LocalDateTime.now());
            promotion.setActive(true);

            promotion.getConditions().forEach(condition -> {
                condition.setPromotion(promotion);
                resolveConditionUom(em, condition);
            });

            promotion.getActions().forEach(action -> {
                action.setPromotion(promotion);
                resolveActionUom(em, action);
            });

            em.persist(promotion);
            return promotion;
        });
    }

    @Override
    public Promotion update(Promotion promotion) {
        throw new UnsupportedOperationException(
                "Promotion details cannot be edited. Only active can be changed."
        );
    }

    @Override
    public List<Promotion> loadActivePromotions(LocalDateTime now) {
        return doInTransaction(em -> em.createQuery("""
                        FROM Promotion p
                        WHERE p.active = true
                          AND p.effectiveDate <= :now
                          AND p.endDate >= :now
                        ORDER BY p.endDate ASC, p.name ASC
                        """, Promotion.class)
                .setParameter("now", now)
                .getResultList());
    }

    @Override
    public Promotion setActive(String promotionId, boolean active) {
        return doInTransaction(em -> {
            Promotion promotion = em.find(Promotion.class, promotionId);
            if (promotion == null) {
                throw new IllegalArgumentException("Promotion not found: " + promotionId);
            }

            // Business rule: promotion details are immutable after creation.
            // Only the active flag can be changed.
            promotion.setActive(active);
            return promotion;
        });
    }

    private void resolveConditionUom(EntityManager em, PromotionCondition condition) {
        if (condition.getProductUom() == null) return;

        condition.setProductUom(findUom(
                em,
                condition.getProductUom().getProduct().getId(),
                condition.getProductUom().getMeasurement().getId()
        ));
    }

    private void resolveActionUom(EntityManager em, PromotionAction action) {
        if (action.getProductUom() == null) return;

        action.setProductUom(findUom(
                em,
                action.getProductUom().getProduct().getId(),
                action.getProductUom().getMeasurement().getId()
        ));
    }

    private UnitOfMeasure findUom(EntityManager em, String productId, String measurementId) {
        UnitOfMeasure uom = em.find(
                UnitOfMeasure.class,
                UnitOfMeasure.UnitOfMeasureId.builder()
                        .product(productId)
                        .measurement(measurementId)
                        .build()
        );

        if (uom == null) {
            throw new IllegalArgumentException(
                    "UnitOfMeasure not found for product=" + productId
                            + ", measurement=" + measurementId
            );
        }

        return uom;
    }
}