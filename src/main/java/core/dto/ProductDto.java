package core.dto;

import core.entities.enums.DosageForm;
import core.entities.enums.ProductCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProductDto {
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
    private String baseMeasurementId;
    private String baseMeasurementName;
    private LocalDateTime creationDate;
}
