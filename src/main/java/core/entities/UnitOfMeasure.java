package core.entities;

import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(
        name = "unit_of_measures",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_uom_product_measurement",
                columnNames = {"product_id", "measurement_name_id"}
        )
)
public class UnitOfMeasure {
    @Id
    @GeneratedId(prefix = "UOM", numberLength = 6, sequenceName = "seq_unit_of_measure_id")
    @Column(name = "unit_of_measure_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "measurement_name_id")
    private MeasurementName measurementName;
    private BigDecimal price;
    @Column(name = "base_unit_conversion_rate")
    private BigDecimal baseUnitConversionRate;
    @Setter(AccessLevel.NONE)
    @Column(name = "base_price_conversion_rate")
    private BigDecimal basePriceConversionRate;

    @PrePersist
    @PreUpdate
    protected void computeDerivedFields() {
        if (baseUnitConversionRate != null && baseUnitConversionRate.compareTo(BigDecimal.ZERO) != 0) {
            // basePriceConversionRate = 1 / baseUnitConversionRate
            basePriceConversionRate = BigDecimal.ONE.divide(baseUnitConversionRate, 10, RoundingMode.HALF_UP);
        } else {
            basePriceConversionRate = BigDecimal.ONE;
        }
    }

    public void setBaseUnitConversionRate(BigDecimal baseUnitConversionRate) {
        this.baseUnitConversionRate = baseUnitConversionRate;
        computeDerivedFields();
    }
}
