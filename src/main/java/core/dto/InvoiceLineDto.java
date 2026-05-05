package core.dto;

import core.entities.enums.InvoiceLineType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InvoiceLineDto {
    private String invoiceId;
    private String productId;
    private String measurementNameId;
    private String measurementNameName;
    private InvoiceLineType type;
    private BigDecimal unitPrice;
    private int quantity;
}
