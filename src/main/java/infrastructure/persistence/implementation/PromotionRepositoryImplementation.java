package infrastructure.persistence.implementation;

import core.entities.*;
import core.entities.enums.ActionType;
import core.entities.enums.ConditionType;
import infrastructure.persistence.PromotionRepository;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PromotionRepositoryImplementation extends AbstractGenericRepositoryImplementation<Promotion, String> implements PromotionRepository {
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
        return doInTransaction(em -> {
            Promotion existing = em.find(Promotion.class, promotion.getId());
            if (existing == null)
                throw new IllegalArgumentException("Promotion not found: " + promotion.getId());

            existing.setName(promotion.getName());
            existing.setDescription(promotion.getDescription());
            existing.setEffectiveDate(promotion.getEffectiveDate());
            existing.setEndDate(promotion.getEndDate());
            existing.setActive(promotion.isActive());

            if (promotion.getConditions() != null) {
                // Safe to clear + re-add — conditions have surrogate IDs,
                // so no composite key identity conflict like UoM
                existing.getConditions().clear();
                promotion.getConditions().forEach(condition -> {
                    condition.setId(null); // force new ID generation
                    condition.setPromotion(existing);
                    resolveConditionUom(em, condition);
                    existing.getConditions().add(condition);
                });
            }

            if (promotion.getActions() != null) {
                existing.getActions().clear();
                promotion.getActions().forEach(action -> {
                    action.setId(null);
                    action.setPromotion(existing);
                    resolveActionUom(em, action);
                    existing.getActions().add(action);
                });
            }

            return existing;
        });
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────────

    private void resolveConditionUom(EntityManager em, PromotionCondition condition) {
        if (condition.getProductUom() == null) return;
        condition.setProductUom(findUom(em,
            condition.getProductUom().getProduct().getId(),
            condition.getProductUom().getMeasurement().getId()));
    }

    private void resolveActionUom(EntityManager em, PromotionAction action) {
        if (action.getProductUom() == null) return;
        action.setProductUom(findUom(em,
            action.getProductUom().getProduct().getId(),
            action.getProductUom().getMeasurement().getId()));
    }

    private UnitOfMeasure findUom(EntityManager em, String productId, String measurementId) {
        UnitOfMeasure uom = em.find(UnitOfMeasure.class,
            UnitOfMeasure.UnitOfMeasureId.builder()
                .product(productId)
                .measurement(measurementId)
                .build());
        if (uom == null)
            throw new IllegalArgumentException(
                "UnitOfMeasure not found for product=" + productId + ", measurement=" + measurementId);
        return uom;
    }

    public static void main(String[] args) {
        PromotionRepository promotionRepository = new PromotionRepositoryImplementation();
        Promotion promotion = Promotion
            .builder()
            .id("PROM000007")
            .name("Buy 3 get 2 free")
            .conditions(List.of(
                PromotionCondition
                    .builder()
                    .type(ConditionType.PRODUCT_ID)
                    .productUom(UnitOfMeasure
                        .builder()
                        .product(Product.builder().id("PRO000002").build())
                        .measurement(Measurement.builder().id("MEA0001").build())
                        .build())
                    .value(BigDecimal.valueOf(3))
                    .build()
            ))
            .actions(List.of(
                PromotionAction
                    .builder()
                    .type(ActionType.PRODUCT_GIFT)
                    .productUom(UnitOfMeasure
                        .builder()
                        .product(Product.builder().id("PRO000001").build())
                        .measurement(Measurement.builder().id("MEA0001").build())
                        .build())
                    .value(BigDecimal.valueOf(2))
                    .build()
            ))
            .build();

        System.out.println(promotionRepository.update(promotion));
    }
}
