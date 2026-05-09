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

    private String        id;
    private String        username;

    // Chỉ dùng khi TẠO MỚI nhân viên — gửi plain text lên server,
    // server sẽ hash trước khi lưu (StaffRepositoryImplementation.create).
    // Khi CẬP NHẬT thì để null — server giữ nguyên password cũ.
    private String        password;

    private String        fullName;
    private String        licenseNumber;
    private String        phoneNumber;
    private String        email;
    private LocalDateTime hireDate;
    private boolean       active;
    private Role          role;
}