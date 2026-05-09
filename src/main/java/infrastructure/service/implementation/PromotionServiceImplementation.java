package infrastructure.service.implementation;

import core.dto.PromotionActionDto;
import core.dto.PromotionConditionDto;
import core.dto.PromotionDto;
import core.entities.Promotion;
import core.entities.enums.*;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.PromotionRepository;
import infrastructure.persistence.implementation.PromotionRepositoryImplementation;
import infrastructure.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PromotionServiceImplementation implements PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionServiceImplementation() {
        promotionRepository = new PromotionRepositoryImplementation();
    }

    @Override
    public PromotionDto create(PromotionDto promotionDto) {
        checkGeneralInfo(promotionDto);
        normalizePromotion(promotionDto);

        Promotion promotion = Mapper.map(promotionDto);
        promotion = promotionRepository.create(promotion);

        return sortActionsForInvoice(Mapper.map(promotion));
    }

    @Override
    public PromotionDto update(PromotionDto promotionDto) {
        throw new UnsupportedOperationException(
                "Promotion details cannot be edited. Only active can be changed."
        );
    }

    @Override
    public PromotionDto findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }

        Promotion promotion = promotionRepository.findById(id);
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion with id " + id + " not found");
        }

        return sortActionsForInvoice(Mapper.map(promotion));
    }

    @Override
    public List<PromotionDto> loadAll() {
        return promotionRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .map(this::sortActionsForInvoice)
                .toList();
    }

    @Override
    public List<PromotionDto> loadActivePromotions() {
        LocalDateTime now = LocalDateTime.now();

        return promotionRepository.loadActivePromotions(now)
                .stream()
                .map(Mapper::map)
                .map(this::sortActionsForInvoice)
                .toList();
    }

    @Override
    public PromotionDto setActive(String promotionId, boolean active) {
        if (promotionId == null || promotionId.isBlank()) {
            throw new IllegalArgumentException("Promotion id cannot be null or blank");
        }

        Promotion updated = promotionRepository.setActive(promotionId, active);
        return sortActionsForInvoice(Mapper.map(updated));
    }

    private PromotionDto sortActionsForInvoice(PromotionDto promotionDto) {
        if (promotionDto.getActions() != null) {
            promotionDto.setActions(
                    promotionDto.getActions()
                            .stream()
                            .sorted(java.util.Comparator.comparingInt(PromotionActionDto::getActionOrder))
                            .toList()
            );
        }

        return promotionDto;
    }

    private void checkGeneralInfo(PromotionDto promotionDto) {
        if (promotionDto == null) {
            throw new IllegalArgumentException("Promotion cannot be null");
        }

        if (promotionDto.getName() == null || promotionDto.getName().isBlank()) {
            throw new IllegalArgumentException("Promotion name cannot be null or blank");
        }

        if (promotionDto.getEffectiveDate() == null) {
            throw new IllegalArgumentException("Effective date cannot be null");
        }

        if (promotionDto.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }

        if (!promotionDto.getEffectiveDate().isBefore(promotionDto.getEndDate())) {
            throw new IllegalArgumentException("Effective date must be before end date");
        }

        if (promotionDto.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }

        if (promotionDto.getConditions() == null || promotionDto.getConditions().isEmpty()) {
            throw new IllegalArgumentException("Promotion must have at least one condition");
        }

        if (promotionDto.getActions() == null || promotionDto.getActions().isEmpty()) {
            throw new IllegalArgumentException("Promotion must have at least one action");
        }

        promotionDto.getConditions().forEach(this::checkCondition);
        checkActions(promotionDto.getActions());
    }

    private void normalizePromotion(PromotionDto promotionDto) {
        promotionDto.getConditions().forEach(this::normalizeCondition);
        promotionDto.getActions().forEach(this::normalizeAction);
    }

    private void checkCondition(PromotionConditionDto condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }

        if (condition.getType() == null) {
            throw new IllegalArgumentException("Condition type cannot be null");
        }

        if (condition.getType() == ConditionType.PRODUCT_ID) {
            throw new IllegalArgumentException("PRODUCT_ID condition is redundant. Use PRODUCT_QTY instead.");
        }

        if (condition.getValue() == null || condition.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Condition value must be greater than 0");
        }

        if (condition.getType() == ConditionType.PRODUCT_QTY) {
            if (condition.getProductId() == null || condition.getProductId().isBlank()) {
                throw new IllegalArgumentException("Product quantity condition must have product id");
            }

            if (condition.getMeasurementId() == null || condition.getMeasurementId().isBlank()) {
                throw new IllegalArgumentException("Product quantity condition must have measurement id");
            }
        }
    }

    private void normalizeCondition(PromotionConditionDto condition) {
        // Business rule: comparator is always GREATER_EQUAL for now.
        condition.setComparator(core.entities.enums.Comparator.GREATER_EQUAL);

        if (condition.getType() == ConditionType.ORDER_SUBTOTAL) {
            condition.setTarget(Target.ORDER_SUBTOTAL);
            condition.setProductId(null);
            condition.setMeasurementId(null);
            condition.setMeasurementName(null);
        }

        if (condition.getType() == ConditionType.PRODUCT_QTY) {
            condition.setTarget(Target.PRODUCT);
        }
    }

    private void checkActions(List<PromotionActionDto> actions) {
        Set<Integer> orders = new HashSet<>();

        for (PromotionActionDto action : actions) {
            checkAction(action);

            if (!orders.add(action.getActionOrder())) {
                throw new IllegalArgumentException("Action order must be unique inside one promotion");
            }
        }
    }

    private void checkAction(PromotionActionDto action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        if (action.getActionOrder() <= 0) {
            throw new IllegalArgumentException("Action order must be positive");
        }

        if (action.getType() == null) {
            throw new IllegalArgumentException("Action type cannot be null");
        }

        if (action.getTarget() == null && action.getType() != ActionType.PRODUCT_GIFT) {
            throw new IllegalArgumentException("Action target cannot be null");
        }

        if (action.getValue() == null || action.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Action value must be greater than 0");
        }

        if (action.getType() == ActionType.PERCENT_DISCOUNT
                && action.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percent discount cannot be greater than 100");
        }

        boolean needsProduct = action.getType() == ActionType.PRODUCT_GIFT
                || action.getTarget() == Target.PRODUCT;

        if (needsProduct) {
            if (action.getProductId() == null || action.getProductId().isBlank()) {
                throw new IllegalArgumentException("Product action must have product id");
            }

            if (action.getMeasurementId() == null || action.getMeasurementId().isBlank()) {
                throw new IllegalArgumentException("Product action must have measurement id");
            }
        }
    }

    private void normalizeAction(PromotionActionDto action) {
        if (action.getType() == ActionType.PRODUCT_GIFT) {
            action.setTarget(Target.PRODUCT);
        }

        if (action.getTarget() == Target.ORDER_SUBTOTAL) {
            action.setProductId(null);
            action.setMeasurementId(null);
            action.setMeasurementName(null);
        }
    }
}