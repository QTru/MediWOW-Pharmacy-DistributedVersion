package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.entities.enums.InvoiceLineType;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "lotAllocations")
@Builder
@EqualsAndHashCode(of = {"invoice", "unitOfMeasure", "type"})

@Entity
@Table(name = "invoice_lines")
@IdClass(InvoiceLine.InvoiceLineId.class)
public class InvoiceLine {
    @Id
    @ManyToOne
    @JoinColumn(name = "invoice_id", columnDefinition = "varchar(20)")
    private Invoice invoice;
    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "uom_product_id",          columnDefinition = "varchar(20)", referencedColumnName = "product_id"),
            @JoinColumn(name = "uom_measurement_name_id", columnDefinition = "varchar(20)", referencedColumnName = "measurement_name_id")
    })
    private UnitOfMeasure unitOfMeasure;
    @Id
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private InvoiceLineType type;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    private int quantity;
    @OneToMany(mappedBy = "invoiceLine")
    @JsonIgnore
    private List<LotAllocation> lotAllocations;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    @EqualsAndHashCode
    public static class InvoiceLineId implements Serializable {
        private String invoice;
        private UnitOfMeasure.UnitOfMeasureId unitOfMeasure;
        private InvoiceLineType type;
    }

    @Transient
    public Product getProduct() {
        return unitOfMeasure == null? null : unitOfMeasure.getProduct();
    }

    public BigDecimal calculateSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal calculateVatAmount() {
        Product product = this.getProduct();
        if (product == null) return BigDecimal.ZERO;
        BigDecimal vatPercent = product.getVat() != null ? product.getVat() : BigDecimal.ZERO;
        BigDecimal vatRate = vatPercent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return calculateSubtotal().multiply(vatRate);
    }

    public BigDecimal calculateTotalAmount() {
        return calculateSubtotal().add(calculateVatAmount());
    }
}
