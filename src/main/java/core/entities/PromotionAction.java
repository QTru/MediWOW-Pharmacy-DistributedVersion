package core.entities;

import core.entities.enums.ActionType;
import core.entities.enums.Target;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "promotion_actions")
public class PromotionAction {
    @Id
    @GeneratedId(prefix = "PROMA", numberLength = 6, sequenceName = "seq_promotion_action_id")
    @Column(name = "promotion_action_id", length = 20, nullable = false)
    private String id;
    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
    @Column(name = "action_order", nullable = false)
    private int actionOrder;
    @Enumerated(EnumType.STRING)
    private ActionType type;
    @Enumerated(EnumType.STRING)
    private Target target;
    private BigDecimal value;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "uom_product_id",     columnDefinition = "varchar(20)", referencedColumnName = "product_id"),
            @JoinColumn(name = "uom_measurement_id", columnDefinition = "varchar(20)", referencedColumnName = "measurement_id")
    })
    private UnitOfMeasure productUom;

    @Transient
    public Product getProduct() {
        return productUom == null? null : productUom.getProduct();
    }
}
