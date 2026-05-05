package core.dto;

import core.entities.enums.Role;
import core.entities.enums.ShiftStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ShiftDto {
    private String id;
    private String staffId;
    private String staffUsername;
    private Role staffRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startMoney;
    private BigDecimal endMoney;
    private BigDecimal systemMoney;
    private ShiftStatus status;
    private String notes;
    private String workStation;
    private String closedByStaffId;
    private String closedByStaffUsername;
    private Role closedByStaffRole;
    private String closingReason;
}
