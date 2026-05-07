package core.dto;

import core.entities.enums.InvoiceLineType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InvoiceLineDto {
    private String invoiceId;
    private String productId;
    private String measurementId;
    private String measurementName;
    private InvoiceLineType type;
    private BigDecimal unitPrice;
    private int quantity;
    private List<LotAllocationDto> lotAllocations;
}
