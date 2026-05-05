package core.dto;

import core.entities.enums.ActionType;
import core.entities.enums.Target;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PromotionActionDto {
    private String id;
    private String promotionId;
    private int actionOrder;
    private ActionType type;
    private Target target;
    private BigDecimal value;
    private String productUomProductId;
    private String productUomMeasurementId;
}
