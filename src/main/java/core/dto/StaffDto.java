package core.dto;

import core.entities.enums.Role;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class StaffDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private LocalDateTime hireDate;
    private boolean active;
    private Role role;
}
