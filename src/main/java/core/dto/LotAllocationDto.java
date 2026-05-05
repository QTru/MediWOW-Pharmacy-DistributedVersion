package core.dto;

import core.entities.enums.InvoiceLineType;
import core.entities.enums.LotStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LotAllocationDto {
    private String invoiceId;
    private String productId;
    private String measurementId;
    private InvoiceLineType invoiceLineType;
    private String lotId;
    private LocalDateTime expiryDate;
    private LotStatus status;
    private int quantity;
}
