package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.entities.enums.DosageForm;
import core.entities.enums.ProductCategory;
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
@ToString(exclude = {"baseUnitOfMeasure", "unitOfMeasures", "lots"})
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedId(prefix = "PRO", numberLength = 6, sequenceName = "seq_product_id")
    @Column(name = "product_id", length = 20, nullable = false)
    private String id;
    @Column(nullable = false, unique = true)
    private String barcode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DosageForm form;
    @Column(nullable = false)
    private String name;
    @Column(name = "short_name", nullable = false)
    private String shortName;
    @Column(nullable = false)
    private String manufacturer;
    @Column(nullable = false)
    private String ingredients;
    @Column(nullable = false)
    private BigDecimal vat;
    private String strength;
    private String description;
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    private List<UnitOfMeasure> unitOfMeasures;
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Lot> lots;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
}
