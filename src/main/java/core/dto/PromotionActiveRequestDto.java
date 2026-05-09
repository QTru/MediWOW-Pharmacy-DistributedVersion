package core.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PromotionActiveRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String promotionId;
    private boolean active;
}