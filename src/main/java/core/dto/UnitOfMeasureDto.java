package core.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UnitOfMeasureDto {
    private String productId;
    private MeasurementDto measurement;
    private BigDecimal price;
    private BigDecimal baseUnitConversionRate;
    private BigDecimal basePriceConversionRate;
}
