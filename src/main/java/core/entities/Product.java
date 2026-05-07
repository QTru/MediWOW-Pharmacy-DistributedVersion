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
    private String barcode;
    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    @Enumerated(EnumType.STRING)
    private DosageForm form;
    private String name;
    @Column(name = "short_name")
    private String shortName;
    private String manufacturer;
    private String ingredients;
    private BigDecimal vat;
    private String strength;
    private String description;
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "base_uom_product_id",     columnDefinition = "varchar(20)", referencedColumnName = "product_id"),
            @JoinColumn(name = "base_uom_measurement_id", columnDefinition = "varchar(20)", referencedColumnName = "measurement_id")
    })
    private UnitOfMeasure baseUnitOfMeasure;
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<UnitOfMeasure> unitOfMeasures;
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Lot> lots;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
}
