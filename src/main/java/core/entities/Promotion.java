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
    @Column(name = "promotion_id", length = 20, nullable = false)
    private String id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    private boolean active;
    @OneToMany(mappedBy = "promotion", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    private List<PromotionCondition> conditions;
    @OneToMany(mappedBy = "promotion", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    private List<PromotionAction> actions;
}
