package infrastructure.service;

import core.dto.ShiftDto;

import java.util.List;

public interface ShiftService {
    ShiftDto create(ShiftDto shiftDto);
    ShiftDto update(ShiftDto shiftDto);
    ShiftDto findById(String id);
    List<ShiftDto> loadAll();
}
