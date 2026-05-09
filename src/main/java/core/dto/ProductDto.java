package core.dto;

import core.entities.enums.DosageForm;
import core.entities.enums.ProductCategory;
import java.io.Serializable;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProductDto implements Serializable {
    private String id;
    private String barcode;
    private ProductCategory category;
    private DosageForm form;
    private String name;
    private String shortName;
    private String manufacturer;
    private String ingredients;
    private BigDecimal vat;
    private String strength;
    private String description;
    private List<UnitOfMeasureDto> unitOfMeasures;
    private LocalDateTime creationDate;
}