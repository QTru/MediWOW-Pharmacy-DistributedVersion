package core.entities;

import core.entities.enums.Comparator;
import core.entities.enums.ConditionType;
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
@Table(name = "promotion_conditions")
public class PromotionCondition {
    @Id
    @GeneratedId(prefix = "PROMC", numberLength = 6, sequenceName = "seq_promotion_condition_id")
    @Column(name = "promotion_condition_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    @Enumerated(EnumType.STRING)
    private ConditionType type;
    @Enumerated(EnumType.STRING)
    private Comparator comparator;
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
