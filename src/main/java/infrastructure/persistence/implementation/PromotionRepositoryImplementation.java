package infrastructure.persistence.implementation;

import core.entities.*;
import core.entities.enums.ActionType;
import core.entities.enums.ConditionType;
import infrastructure.persistence.PromotionRepository;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

public class PromotionRepositoryImplementation extends AbstractGenericRepositoryImplementation<Promotion, String> implements PromotionRepository {
    public PromotionRepositoryImplementation() {
        super(Promotion.class);
    }

    @Override
    public Promotion create(Promotion promotion) {
        return doInTransaction(em -> {
            if (promotion.getConditions() == null || promotion.getConditions().isEmpty())
                throw new IllegalArgumentException("Promotion must have at least one condition");
            if (promotion.getActions() == null || promotion.getActions().isEmpty())
                throw new IllegalArgumentException("Promotion must have at least one action");

            promotion.setConditions(promotion.getConditions().stream().map(condition -> {
                condition.setPromotion(promotion);
                if (condition.getProductUom() != null)
                    condition.setProductUom(resolveUom(em, condition.getProductUom()));
                return condition;
            }).toList());

            promotion.setActions(promotion.getActions().stream().map(action -> {
                action.setPromotion(promotion);
                if (action.getProductUom() != null)
                    action.setProductUom(resolveUom(em, action.getProductUom()));
                return action;
            }).toList());

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

            // Update basic fields
            existing.setName(promotion.getName());
            existing.setDescription(promotion.getDescription());
            existing.setEffectiveDate(promotion.getEffectiveDate());
            existing.setEndDate(promotion.getEndDate());
            existing.setActive(promotion.isActive());

            // Update conditions — orphanRemoval tự xóa conditions bị remove
            if (promotion.getConditions() != null) {
                List<PromotionCondition> updatedConditions = promotion.getConditions().stream().map(condition -> {
                    condition.setPromotion(existing);
                    if (condition.getProductUom() != null)
                        condition.setProductUom(resolveUom(em, condition.getProductUom()));
                    return condition;
                }).toList();
                existing.getConditions().clear();            // trigger orphanRemoval
                existing.getConditions().addAll(updatedConditions);
            }

            // Update actions — same pattern
            if (promotion.getActions() != null) {
                List<PromotionAction> updatedActions = promotion.getActions().stream().map(action -> {
                    action.setPromotion(existing);
                    if (action.getProductUom() != null)
                        action.setProductUom(resolveUom(em, action.getProductUom()));
                    return action;
                }).toList();
                existing.getActions().clear();               // trigger orphanRemoval
                existing.getActions().addAll(updatedActions);
            }

            return existing; // đã managed, JPA tự flush
        });
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private UnitOfMeasure resolveUom(EntityManager em, UnitOfMeasure uom) {
        if (uom.getProduct() == null || uom.getProduct().getId() == null)
            throw new IllegalArgumentException("Product UOM must have a product");
        if (uom.getMeasurement() == null || uom.getMeasurement().getId() == null)
            throw new IllegalArgumentException("Product UOM must have a measurement");

        UnitOfMeasure.UnitOfMeasureId uomId = UnitOfMeasure.UnitOfMeasureId.builder()
                .product(uom.getProduct().getId())
                .measurement(uom.getMeasurement().getId())
                .build();

        UnitOfMeasure existing = em.find(UnitOfMeasure.class, uomId);
        if (existing == null)
            throw new IllegalArgumentException("UnitOfMeasure not found — product: "
                    + uom.getProduct().getId() + ", measurement: " + uom.getMeasurement().getId());

        return existing; // em.find() trả về managed entity trực tiếp
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
