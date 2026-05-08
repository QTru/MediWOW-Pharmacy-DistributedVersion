package core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = {"invoiceLine", "lot"})

@Entity
@Table(name = "lot_allocations")
@IdClass(LotAllocation.LotAllocationId.class)
public class LotAllocation {
    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "invoice_id", nullable = false,              columnDefinition = "varchar(20)", referencedColumnName = "invoice_id"),
            @JoinColumn(name = "uom_product_id", nullable = false,          columnDefinition = "varchar(20)", referencedColumnName = "uom_product_id"),
            @JoinColumn(name = "uom_measurement_id", nullable = false,      columnDefinition = "varchar(20)", referencedColumnName = "uom_measurement_id"),
            @JoinColumn(name = "invoice_line_type", nullable = false,       columnDefinition = "varchar(20)", referencedColumnName = "type")
    })
    @JsonBackReference("invoiceLine-lotAllocations")
    private InvoiceLine invoiceLine;
    @Id
    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false, columnDefinition = "varchar(20)")
    private Lot lot;
    @Column(nullable = false)
    private int quantity;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    @EqualsAndHashCode
    public static class LotAllocationId implements Serializable {
        private InvoiceLine.InvoiceLineId invoiceLine;
        private String lot;
    }
}
