package core.dto;

import core.entities.enums.LotStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LotDto {
    private String id;
    private int batchNumber;
    private String productId;
    private int quantity;
    private BigDecimal rawPrice;
    private LocalDateTime expiryDate;
    private LotStatus status;
}
