package core.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PromotionDto implements Serializable {
    private String id;
    private String name;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime effectiveDate;
    private LocalDateTime endDate;
    private boolean active;
    private List<PromotionConditionDto> conditions;
    private List<PromotionActionDto> actions;
}
