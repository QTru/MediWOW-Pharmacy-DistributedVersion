package infrastructure.service.implementation;

import core.dto.ShiftDto;
import core.entities.Shift;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.ShiftRepository;
import infrastructure.persistence.implementation.ShiftRepositoryImplementation;
import infrastructure.service.ShiftService;

import java.math.BigDecimal;
import java.util.List;

public class ShiftServiceImplementation implements ShiftService {
    private final ShiftRepository shiftRepository;

    public ShiftServiceImplementation() {
        shiftRepository = new ShiftRepositoryImplementation();
    }

    @Override
    public ShiftDto create(ShiftDto shiftDto) {
        checkGeneralInfo(shiftDto);

        Shift shift = Mapper.map(shiftDto);
        shift = shiftRepository.create(shift);
        return Mapper.map(shift);
    }

    @Override
    public ShiftDto update(ShiftDto shiftDto) {
        if (shiftDto.getId() == null || shiftDto.getId().isBlank())
            throw new IllegalArgumentException("Shift id cannot be null or blank");
        checkGeneralInfo(shiftDto);
        checkClosingInfo(shiftDto);

        Shift shift = Mapper.map(shiftDto);
        shift = shiftRepository.update(shift);
        return Mapper.map(shift);
    }

    @Override
    public ShiftDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Shift shift = shiftRepository.findById(id);
        if (shift == null)
            throw new IllegalArgumentException("Shift with id " + id + " not found");

        return Mapper.map(shift);
    }

    @Override
    public List<ShiftDto> loadAll() {
        return shiftRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .toList();
    }

    private void checkGeneralInfo(ShiftDto shiftDto) {
        if (shiftDto.getStaffId() == null || shiftDto.getStaffId().isEmpty())
            throw new IllegalArgumentException("Staff ID cannot be null or empty");
        if (shiftDto.getStartMoney() == null || shiftDto.getStartMoney().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Start money cannot be null or negative");
        if (shiftDto.getWorkStation() == null || shiftDto.getWorkStation().isEmpty())
            throw new IllegalArgumentException("Work station cannot be null or empty");
    }

    private void checkClosingInfo(ShiftDto shiftDto) {
        if (shiftDto.getEndMoney() == null || shiftDto.getEndMoney().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("End money cannot be null or negative");
        if (shiftDto.getClosedByStaffId() == null || shiftDto.getClosedByStaffId().isEmpty())
            throw new IllegalArgumentException("Closed by staff ID cannot be null or empty");
    }

    public static void main(String[] args) {
        ShiftService shiftService = new ShiftServiceImplementation();

//        ShiftDto shiftDto = ShiftDto
//            .builder()
//            .staffId("STA0001")
//            .startMoney(BigDecimal.valueOf(10_000_000))
//            .workStation("workstation1")
//            .build();
//
//        shiftDto = shiftService.create(shiftDto);
//        System.out.println(shiftDto);

        ShiftDto shiftDto = shiftService.findById("SHI000007");
        shiftDto.setEndMoney(BigDecimal.valueOf(12_000_000));
        shiftDto.setClosedByStaffId("STA0001");
        shiftDto = shiftService.update(shiftDto);
        System.out.println(shiftDto);
    }
}
