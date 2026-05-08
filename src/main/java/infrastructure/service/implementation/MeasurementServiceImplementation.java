package infrastructure.service.implementation;

import core.dto.MeasurementDto;
import core.entities.Measurement;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.MeasurementRepository;
import infrastructure.persistence.implementation.MeasurementRepositoryImplementation;
import infrastructure.service.MeasurementService;

import java.util.List;

public class MeasurementServiceImplementation implements MeasurementService {
    private final MeasurementRepository measurementRepository;

    public MeasurementServiceImplementation() {
        measurementRepository = new MeasurementRepositoryImplementation();
    }

    @Override
    public MeasurementDto create(MeasurementDto measurementDto) {
        if (measurementDto.getName() == null || measurementDto.getName().isBlank())
            throw new IllegalArgumentException("Measurement name cannot be null or blank");

        Measurement measurement = Mapper.map(measurementDto, Measurement.class);
        measurement = measurementRepository.create(measurement);
        return Mapper.map(measurement, MeasurementDto.class);
    }

    @Override
    public MeasurementDto update(MeasurementDto measurementDto) {
        if (measurementDto.getId() == null || measurementDto.getId().isBlank())
            throw new IllegalArgumentException("Measurement id cannot be null or blank");
        if (measurementDto.getName() == null || measurementDto.getName().isBlank())
            throw new IllegalArgumentException("Measurement name cannot be null or blank");

        Measurement measurement = Mapper.map(measurementDto, Measurement.class);
        measurement = measurementRepository.update(measurement);
        return Mapper.map(measurement, MeasurementDto.class);
    }

    @Override
    public MeasurementDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Measurement measurement = measurementRepository.findById(id);
        if (measurement == null)
            throw new IllegalArgumentException("Measurement with id " + id + " not found");

        return Mapper.map(measurement, MeasurementDto.class);
    }

    @Override
    public List<MeasurementDto> loadAll() {
        return measurementRepository.loadAll()
                .stream()
                .map(measurement -> Mapper.map(measurement, MeasurementDto.class))
                .toList();
    }

    @Override
    public MeasurementDto findByName(String name) {
        return null;
    }
}
