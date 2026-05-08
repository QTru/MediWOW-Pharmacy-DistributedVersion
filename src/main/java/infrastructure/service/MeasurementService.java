package infrastructure.service;

import core.dto.MeasurementDto;

import java.util.List;

public interface MeasurementService {
    MeasurementDto create(MeasurementDto measurementDto);
    MeasurementDto update(MeasurementDto measurementDto);
    MeasurementDto findById(String id);
    List<MeasurementDto> loadAll();
    MeasurementDto findByName(String name);
}
