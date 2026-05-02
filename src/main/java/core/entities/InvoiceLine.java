package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.entities.enums.InvoiceLineType;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "lotAllocations")
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(
        name = "invoice_lines",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoiceline_invoice_uom_type",
                columnNames = {"invoice_id", "unit_of_measure_id", "type"}
        )
)
public class InvoiceLine {
    @Id
    @GeneratedId(prefix = "INL", numberLength = 6)
    @Column(name = "invoice_line_id")
    private String id;
    @ManyToOne
    @JoinColumn(name =  "invoice_id")
    private Invoice invoice;
    @ManyToOne
    @JoinColumn(name = "unit_of_measure_id")
    private UnitOfMeasure unitOfMeasure;
    @Enumerated(EnumType.STRING)
    private InvoiceLineType type;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    private int quantity;
    @OneToMany(mappedBy = "invoiceLine")
    @JsonIgnore
    private List<LotAllocation> lotAllocations;

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
