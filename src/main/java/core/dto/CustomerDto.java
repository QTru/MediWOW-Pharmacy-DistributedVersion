package core.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CustomerDto implements Serializable {
    private String id;
    private String name;
    private String phoneNumber;
    private String address;
    private LocalDateTime creationDate;
}
