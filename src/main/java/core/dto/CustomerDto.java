package core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CustomerDto {
    private String id;
    private String name;
    private String phoneNumber;
    private String address;
    private LocalDateTime creationDate;
}
