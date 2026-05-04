package core.entities;

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
            @JoinColumn(name = "invoice_id",              columnDefinition = "varchar(20)", referencedColumnName = "invoice_id"),
            @JoinColumn(name = "uom_product_id",          columnDefinition = "varchar(20)", referencedColumnName = "uom_product_id"),
            @JoinColumn(name = "uom_measurement_name_id", columnDefinition = "varchar(20)", referencedColumnName = "uom_measurement_name_id"),
            @JoinColumn(name = "type",                    columnDefinition = "varchar(20)", referencedColumnName = "type")
    })
    private InvoiceLine invoiceLine;
    @Id
    @ManyToOne
    @JoinColumn(name = "lot_id", columnDefinition = "varchar(20)")
    private Lot lot;
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
