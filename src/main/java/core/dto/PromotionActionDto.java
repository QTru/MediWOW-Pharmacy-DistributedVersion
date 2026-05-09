package core.dto;

import core.entities.enums.ActionType;
import core.entities.enums.Target;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PromotionActionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String promotionId;
    private int actionOrder;
    private ActionType type;
    private Target target;
    private BigDecimal value;

    private String productId;
    private String productName;
    private String measurementId;
    private String measurementName;

    private BigDecimal productUomBaseUnitConversionRate;
    private boolean productUomBaseUnit;
    private BigDecimal productUomPrice;
}