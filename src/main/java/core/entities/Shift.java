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
    @GeneratedId(prefix = "SHI", numberLength = 6, sequenceName = "seq_shift_id")
    @Column(name = "shift_id", length = 20, nullable = false)
    private String id;
    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "start_money", nullable = false)
    private BigDecimal startMoney;
    @Column(name = "end_money", nullable = false)
    private BigDecimal endMoney;
    @Column(name = "system_money", nullable = false)
    private BigDecimal systemMoney;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;
    private String notes;
    @Column(name = "work_station", nullable = false)
    private String workStation;
    @ManyToOne
    @JoinColumn(name = "closed_by_staff_id", nullable = false)
    private Staff closedByStaff;
    @Column(name = "closing_reason", nullable = false)
    private String closingReason;
}
