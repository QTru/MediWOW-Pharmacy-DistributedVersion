package infrastructure.service.implementation;

import core.dto.PromotionActionDto;
import core.dto.PromotionConditionDto;
import core.dto.PromotionDto;
import core.entities.enums.ActionType;
import core.entities.enums.ConditionType;
import core.entities.enums.Target;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromotionServiceImplementationTest {

    private final PromotionServiceImplementation service =
            new PromotionServiceImplementation();

    private PromotionDto validPromotion() {
        return PromotionDto.builder()
                .name("Test Promotion")
                .effectiveDate(LocalDateTime.now().plusHours(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .conditions(List.of(
                        PromotionConditionDto.builder()
                                .type(ConditionType.ORDER_SUBTOTAL)
                                .target(Target.ORDER_SUBTOTAL)
                                .value(BigDecimal.valueOf(500000))
                                .build()
                ))
                .actions(List.of(
                        PromotionActionDto.builder()
                                .actionOrder(1)
                                .type(ActionType.PERCENT_DISCOUNT)
                                .target(Target.ORDER_SUBTOTAL)
                                .value(BigDecimal.valueOf(10))
                                .build()
                ))
                .build();
    }

    @Test
    void cannotCreatePromotionWithoutName() {
        PromotionDto dto = validPromotion();
        dto.setName("");

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void cannotCreatePromotionWhenEffectiveDateAfterEndDate() {
        PromotionDto dto = validPromotion();
        dto.setEffectiveDate(LocalDateTime.now().plusDays(10));
        dto.setEndDate(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void cannotCreatePromotionWithoutConditions() {
        PromotionDto dto = validPromotion();
        dto.setConditions(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void cannotCreatePromotionWithoutActions() {
        PromotionDto dto = validPromotion();
        dto.setActions(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void conditionValueMustBeGreaterThanZero() {
        PromotionDto dto = validPromotion();
        dto.getConditions().get(0).setValue(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void actionValueMustBeGreaterThanZero() {
        PromotionDto dto = validPromotion();
        dto.getActions().get(0).setValue(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void actionOrderMustBePositive() {
        PromotionDto dto = validPromotion();
        dto.getActions().get(0).setActionOrder(0);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void duplicateActionOrderIsRejected() {
        PromotionDto dto = validPromotion();

        dto.setActions(List.of(
                PromotionActionDto.builder()
                        .actionOrder(1)
                        .type(ActionType.PERCENT_DISCOUNT)
                        .target(Target.ORDER_SUBTOTAL)
                        .value(BigDecimal.valueOf(10))
                        .build(),
                PromotionActionDto.builder()
                        .actionOrder(1)
                        .type(ActionType.FIXED_DISCOUNT)
                        .target(Target.ORDER_SUBTOTAL)
                        .value(BigDecimal.valueOf(10000))
                        .build()
        ));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void productQtyConditionRequiresProductAndMeasurement() {
        PromotionDto dto = validPromotion();

        dto.setConditions(List.of(
                PromotionConditionDto.builder()
                        .type(ConditionType.PRODUCT_QTY)
                        .target(Target.PRODUCT)
                        .value(BigDecimal.ONE)
                        .build()
        ));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void percentDiscountCannotBeGreaterThan100() {
        PromotionDto dto = validPromotion();
        dto.getActions().get(0).setValue(BigDecimal.valueOf(101));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }
}