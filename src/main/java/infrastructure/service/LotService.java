package infrastructure.service;

import core.dto.LotDto;

import java.util.List;

public interface LotService {
    LotDto create(LotDto lotDto);
    LotDto update(LotDto lotDto);
    LotDto findById(String id);
    List<LotDto> loadAll();

    List<LotDto> findAvailableLotsByProductId(String productId);
}