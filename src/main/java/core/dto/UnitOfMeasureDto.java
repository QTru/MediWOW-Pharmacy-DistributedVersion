package core.dto;

import java.io.Serializable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UnitOfMeasureDto implements Serializable {
    private String productId;
    private MeasurementDto measurement;
    private boolean baseUnit;
    private BigDecimal price;
    private BigDecimal baseUnitConversionRate;
    private BigDecimal basePriceConversionRate;
}