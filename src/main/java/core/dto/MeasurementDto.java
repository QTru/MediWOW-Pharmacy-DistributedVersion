package core.dto;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MeasurementDto implements Serializable {
    private String id;
    private String name;
}