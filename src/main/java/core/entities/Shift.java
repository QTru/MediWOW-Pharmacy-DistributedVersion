package core.entities;

import core.entities.enums.ShiftStatus;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedId(prefix = "SHI", numberLength = 6)
    @Column(name = "shift_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "start_money")
    private BigDecimal startMoney;
    @Column(name = "end_money")
    private BigDecimal endMoney;
    @Column(name = "system_money")
    private BigDecimal systemMoney;
    @Enumerated(EnumType.STRING)
    private ShiftStatus status;
    private String notes;
    @Column(name = "work_station")
    private String workStation;
    @ManyToOne
    @JoinColumn(name = "closed_by_staff_id")
    private Staff closedByStaff;
    @Column(name = "closing_reason")
    private String closingReason;
}
