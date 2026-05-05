package core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PromotionDto {
    private String id;
    private String name;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime effectiveDate;
    private LocalDateTime endDate;
    private boolean active;
}
