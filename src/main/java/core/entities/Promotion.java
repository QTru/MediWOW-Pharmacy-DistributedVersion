package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"conditions", "actions"})
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedId(prefix = "PROM", numberLength = 6, sequenceName = "seq_promotion_id")
    @Column(name = "promotion_id")
    private String id;
    private String name;
    private String description;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    private boolean active;
    @OneToMany(mappedBy = "promotion")
    @JsonIgnore
    private List<PromotionCondition> conditions;
    @OneToMany(mappedBy = "promotion")
    @JsonIgnore
    private List<PromotionAction> actions;
}
