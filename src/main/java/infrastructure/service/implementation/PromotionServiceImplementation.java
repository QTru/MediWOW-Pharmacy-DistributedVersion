package infrastructure.service.implementation;

import core.dto.PromotionActionDto;
import core.dto.PromotionConditionDto;
import core.dto.PromotionDto;
import core.entities.Promotion;
import core.entities.enums.ActionType;
import core.entities.enums.Comparator;
import core.entities.enums.ConditionType;
import core.entities.enums.Target;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.PromotionRepository;
import infrastructure.persistence.implementation.PromotionRepositoryImplementation;
import infrastructure.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PromotionServiceImplementation implements PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionServiceImplementation() {
        promotionRepository = new PromotionRepositoryImplementation();
    }

    @Override
    public PromotionDto create(PromotionDto promotionDto) {
        checkGeneralInfo(promotionDto);
        if (promotionDto.getEffectiveDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Effective date cannot be in the past");

        Promotion promotion = Mapper.map(promotionDto);
        promotion = promotionRepository.create(promotion);
        return Mapper.map(promotion);
    }

    @Override
    public PromotionDto update(PromotionDto promotionDto) {
        if (promotionDto.getId() == null || promotionDto.getId().isBlank())
            throw new IllegalArgumentException("Promotion id cannot be null or blank");

        checkGeneralInfo(promotionDto);

        Promotion promotion = Mapper.map(promotionDto);
        promotion = promotionRepository.update(promotion);
        return Mapper.map(promotion);
    }

    @Override
    public PromotionDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Promotion promotion = promotionRepository.findById(id);
        if (promotion == null)
            throw new IllegalArgumentException("Promotion with id " + id + " not found");

        return Mapper.map(promotion);
    }

    @Override
    public List<PromotionDto> loadAll() {
        return promotionRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .toList();
    }

    // ─── VALIDATION ───────────────────────────────────────────────────────────────

    private void checkGeneralInfo(PromotionDto promotionDto) {
        if (promotionDto.getName() == null || promotionDto.getName().isBlank())
            throw new IllegalArgumentException("Promotion name cannot be null or blank");
        if (promotionDto.getEffectiveDate() == null)
            throw new IllegalArgumentException("Effective date cannot be null");
        if (promotionDto.getEndDate() == null)
            throw new IllegalArgumentException("End date cannot be null");
        if (!promotionDto.getEffectiveDate().isBefore(promotionDto.getEndDate()))
            throw new IllegalArgumentException("Effective date must be before end date");

        if (promotionDto.getConditions() == null || promotionDto.getConditions().isEmpty())
            throw new IllegalArgumentException("Promotion must have at least one condition");

        promotionDto.getConditions().forEach(this::checkCondition);

        if (promotionDto.getActions() == null || promotionDto.getActions().isEmpty())
            throw new IllegalArgumentException("Promotion must have at least one action");

        promotionDto.getActions().forEach(this::checkAction);
    }

    private void checkCondition(PromotionConditionDto condition) {
        if (condition.getType() == null)
            throw new IllegalArgumentException("Condition type cannot be null");
        if (condition.getComparator() == null)
            throw new IllegalArgumentException("Condition comparator cannot be null");
        if (condition.getTarget() == null)
            throw new IllegalArgumentException("Condition target cannot be null");
        if (condition.getValue() == null || condition.getValue().signum() < 0)
            throw new IllegalArgumentException("Condition value must be non-negative");
    }

    private void checkAction(PromotionActionDto action) {
        if (action.getType() == null)
            throw new IllegalArgumentException("Action type cannot be null");
        if (action.getActionOrder() <= 0)
            throw new IllegalArgumentException("Action order must be positive");
        if (action.getTarget() == null)
            throw new IllegalArgumentException("Action target cannot be null");
        if (action.getValue() == null || action.getValue().signum() < 0)
            throw new IllegalArgumentException("Action value must be non-negative");
    }

    public static void main(String[] args) {
        PromotionService promotionService = new PromotionServiceImplementation();

//        PromotionDto promotionDto = PromotionDto
//            .builder()
//            .name("Buy 2 get 1 free")
//            .effectiveDate(LocalDateTime.now().plusDays(1))
//            .endDate(LocalDateTime.now().plusDays(10))
//            .conditions(List.of(PromotionConditionDto
//                .builder()
//                .type(ConditionType.PRODUCT_ID)
//                .comparator(Comparator.EQUAL)
//                .target(Target.ORDER_SUBTOTAL)
//                .value(BigDecimal.valueOf(2))
//                .productId("PRO000001")
//                .measurementId("MEA0001")
//                .build()))
//            .actions(List.of(PromotionActionDto
//                .builder()
//                .type(ActionType.PRODUCT_GIFT)
//                .actionOrder(1)
//                .target(Target.ORDER_SUBTOTAL)
//                .value(BigDecimal.valueOf(2))
//                .productId("PRO000002")
//                .measurementId("MEA0001")
//                .build()))
//            .build();
//
//        PromotionDto createdPromotion = promotionService.create(promotionDto);
//        System.out.println(createdPromotion);

        PromotionDto promotionDto = promotionService.findById("PROM000007");
        promotionDto.setEffectiveDate(LocalDateTime.now().minusDays(10));
        promotionDto.setEndDate(LocalDateTime.now().minusDays(1));
        promotionDto.setConditions(List.of(PromotionConditionDto
            .builder()
            .type(ConditionType.PRODUCT_ID)
            .comparator(Comparator.GREATER_EQUAL)
            .target(Target.ORDER_SUBTOTAL)
            .value(BigDecimal.valueOf(2))
            .productId("PRO000002")
            .measurementId("MEA0001")
            .build()));
        promotionDto.setActions(List.of(PromotionActionDto
                .builder()
                .type(ActionType.PRODUCT_GIFT)
                .actionOrder(1)
                .target(Target.ORDER_SUBTOTAL)
                .value(BigDecimal.valueOf(1))
                .productId("PRO000001")
                .measurementId("MEA0001")
                .build()));

        PromotionDto updatedPromotion = promotionService.update(promotionDto);
        System.out.println(updatedPromotion);
    }
}