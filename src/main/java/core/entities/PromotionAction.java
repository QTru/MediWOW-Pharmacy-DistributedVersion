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
    @Column(name = "promotion_action_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    @Column(name = "action_order")
    private int actionOrder;
    @Enumerated(EnumType.STRING)
    private ActionType type;
    @Enumerated(EnumType.STRING)
    private Target target;
    private BigDecimal value;
    @ManyToOne
    @JoinColumn(name = "unit_of_measure_id")
    private UnitOfMeasure productUom;

    @Transient
    public Product getProduct() {
        return productUom == null? null : productUom.getProduct();
    }
}
