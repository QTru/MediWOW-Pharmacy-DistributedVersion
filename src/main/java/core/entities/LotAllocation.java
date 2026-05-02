package core.entities;

import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(
        name = "lot_allocations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lotallocation_invoiceline_lot",
                columnNames = {"invoice_line_id", "lot_id"}
        )
)
public class LotAllocation {
    @Id
    @GeneratedId(prefix = "LOA", numberLength = 6)
    @Column(name = "lot_allocation_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "invoice_line_id")
    private InvoiceLine invoiceLine;
    @ManyToOne
    @JoinColumn(name = "lot_id")
    private Lot lot;
    private int quantity;
}
