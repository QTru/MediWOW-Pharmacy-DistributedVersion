package infrastructure.service;

import core.dto.PromotionDto;

import java.util.List;

public interface PromotionService {
    PromotionDto create(PromotionDto promotionDto);

    PromotionDto update(PromotionDto promotionDto);

    PromotionDto findById(String id);

    List<PromotionDto> loadAll();

    List<PromotionDto> loadActivePromotions();

    PromotionDto setActive(String promotionId, boolean active);
}