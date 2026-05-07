package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.entities.enums.Role;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"password", "shifts", "closedShifts"})
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "staffs")
public class Staff {
    @Id
    @GeneratedId(prefix = "STA", numberLength = 4, sequenceName = "seq_staff_id")
    @Column(name = "staff_id", length = 20, nullable = false)
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "license_number")
    private String licenseNumber;
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    private String email;
    @Column(name = "hire_date")
    private LocalDateTime hireDate;
    private boolean active;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "staff")
    @JsonIgnore
    private List<Shift> shifts;
    @OneToMany(mappedBy = "closedByStaff")
    @JsonIgnore
    private List<Shift> closedShifts;
}
