package core.dto;

import core.entities.enums.Comparator;
import core.entities.enums.ConditionType;
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
public class PromotionConditionDto implements Serializable {
    private String id;
    private String promotionId;
    private ConditionType type;
    private Comparator comparator;
    private Target target;
    private BigDecimal value;
    private String productId;
    private String measurementId;
    private String measurementName;
}
