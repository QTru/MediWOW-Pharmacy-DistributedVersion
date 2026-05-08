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
@EqualsAndHashCode(of = {"product", "measurement"})

@Entity
@Table(name = "unit_of_measures")
@IdClass(UnitOfMeasure.UnitOfMeasureId.class)
public class UnitOfMeasure {
    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "varchar(20)")
    private Product product;
    @Id
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "measurement_id", nullable = false, columnDefinition = "varchar(20)")
    private Measurement measurement;
    @Column(name = "base_unit", nullable = false)
    private boolean baseUnit = false;
    private BigDecimal price;
    @Column(name = "base_unit_conversion_rate", nullable = false)
    private BigDecimal baseUnitConversionRate;
    @Setter(AccessLevel.NONE)
    @Column(name = "base_price_conversion_rate")
    private BigDecimal basePriceConversionRate;

    @lombok.Builder
    public UnitOfMeasure(Product product, Measurement measurement, boolean baseUnit,
                         BigDecimal price, BigDecimal baseUnitConversionRate) {
        this.product = product;
        this.measurement = measurement;
        this.baseUnit = baseUnit;
        this.price = price;
        this.baseUnitConversionRate = baseUnit ? BigDecimal.ONE : baseUnitConversionRate;
        setBaseUnitConversionRate(baseUnitConversionRate);
    }

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
        if (isBaseUnit())
            baseUnitConversionRate = BigDecimal.ONE;
        if (baseUnitConversionRate != null && baseUnitConversionRate.compareTo(BigDecimal.ZERO) != 0) {
            // basePriceConversionRate = 1 / baseUnitConversionRate
            basePriceConversionRate = BigDecimal.ONE.divide(baseUnitConversionRate, 10, RoundingMode.HALF_UP);
        } else {
            basePriceConversionRate = BigDecimal.ONE;
        }
    }

    public void setBaseUnitConversionRate(BigDecimal baseUnitConversionRate) {
        if (isBaseUnit())
            this.baseUnitConversionRate = BigDecimal.ONE;
        else
            this.baseUnitConversionRate = baseUnitConversionRate;
        computeDerivedFields();
    }

    public void setBaseUnit(boolean baseUnit) {
        this.baseUnit = baseUnit;
        if (baseUnit) {
            this.baseUnitConversionRate = BigDecimal.ONE;
            computeDerivedFields();
        }
    }
}
