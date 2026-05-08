package infrastructure.service;

import core.dto.StaffDto;

import java.util.List;

public interface StaffService {
    StaffDto create(StaffDto staffDto);
    StaffDto update(StaffDto staffDto);
    StaffDto findById(String id);
    List<StaffDto> loadAll();
}
