package core.dto;

import core.entities.enums.InvoiceType;
import core.entities.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "invoiceLines")
@Builder
public class InvoiceDto {
    private String id;
    private InvoiceType type;
    private String note;
    private LocalDateTime creationDate;
    private String creatorId;
    private String creatorFullName;
    private String shiftId;
    private String prescriptionCode;
    private String customerPhoneNumber;
    private List<InvoiceLineDto> invoiceLines;
    private String promotionId;
    private String promotionName;
    private PaymentMethod paymentMethod;
    private String referencedInvoiceId;
    private String parentInvoiceId;
}
