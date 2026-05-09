package infrastructure.persistence;

import core.entities.Promotion;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends GenericRepository<Promotion, String> {
    List<Promotion> loadActivePromotions(LocalDateTime now);

    Promotion setActive(String promotionId, boolean active);
}