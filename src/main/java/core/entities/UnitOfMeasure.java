package core.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = {"product", "measurement"})

@Entity
@Table(name = "unit_of_measures")
@IdClass(UnitOfMeasure.UnitOfMeasureId.class)
public class UnitOfMeasure {
    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", columnDefinition = "varchar(20)")
    private Product product;
    @Id
    @ManyToOne
    @JoinColumn(name = "measurement_id", columnDefinition = "varchar(20)")
    private Measurement measurement;
    private BigDecimal price;
    @Column(name = "base_unit_conversion_rate")
    private BigDecimal baseUnitConversionRate;
    @Setter(AccessLevel.NONE)
    @Column(name = "base_price_conversion_rate")
    private BigDecimal basePriceConversionRate;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    @EqualsAndHashCode
    public static class UnitOfMeasureId implements Serializable {
        private String product;
        private String measurement;
    }

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
