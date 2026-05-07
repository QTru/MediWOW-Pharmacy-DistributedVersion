package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.entities.enums.LotStatus;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "lotAllocations")
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "lots")
public class Lot {
    @Id
    @GeneratedId(prefix = "LOT", numberLength = 6, sequenceName = "seq_lot_id")
    @Column(name = "lot_id", length = 20, nullable = false)
    private String id;
    @Column(name = "batch_number")
    private int batchNumber;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private int quantity;
    @Column(name = "raw_price")
    private BigDecimal rawPrice;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    @Enumerated(EnumType.STRING)
    private LotStatus status;
    @OneToMany(mappedBy = "lot")
    @JsonIgnore
    private List<LotAllocation> lotAllocations;
}
