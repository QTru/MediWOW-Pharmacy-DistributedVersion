package infrastructure.service.implementation;

import core.dto.LotDto;
import core.entities.Lot;
import core.entities.enums.LotStatus;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.LotRepository;
import infrastructure.persistence.implementation.LotRepositoryImplementation;
import infrastructure.service.LotService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class LotServiceImplementation implements LotService {
    private final LotRepository lotRepository;

    public LotServiceImplementation() {
        this.lotRepository = new LotRepositoryImplementation();
    }

    @Override
    public LotDto create(LotDto lotDto) {
        checkGeneralInfo(lotDto);

        Lot lot = Mapper.map(lotDto);
        lot = lotRepository.create(lot);
        return Mapper.map(lot);
    }

    @Override
    public LotDto update(LotDto lotDto) {
        if (lotDto.getId() == null || lotDto.getId().isBlank())
            throw new IllegalArgumentException("Lot id cannot be null or blank");
        checkGeneralInfo(lotDto);

        Lot lot = Mapper.map(lotDto);
        lot = lotRepository.update(lot);
        return null;
    }

    @Override
    public LotDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Lot lot = lotRepository.findById(id);
        if (lot == null)
            throw new IllegalArgumentException("Lot with id " + id + " not found");

        return Mapper.map(lot);
    }

    @Override
    public List<LotDto> loadAll() {
        return lotRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .toList();
    }

    private void checkGeneralInfo(LotDto lotDto) {
        if (lotDto.getBatchNumber() < 1)
            throw new IllegalArgumentException("Batch number must be greater than 0");
        if (lotDto.getQuantity() < 1)
            throw new IllegalArgumentException("Quantity must be greater than 0");
        if (lotDto.getRawPrice() == null || lotDto.getRawPrice().signum() <= 0)
            throw new IllegalArgumentException("Raw price must be greater than 0");
        if (lotDto.getExpiryDate() == null)
            throw new IllegalArgumentException("Expiry date cannot be null");
        if (lotDto.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        if (lotDto.getStatus() == null)
            throw new IllegalArgumentException("Status cannot be null");
    }

    public static void main(String[] args) {
            LotService lotService = new LotServiceImplementation();

            LotDto lotDto = LotDto
                .builder()
                .batchNumber(1)
                .productId("PRO000001")
                .quantity(100)
                .rawPrice(BigDecimal.valueOf(500))
                .expiryDate(LocalDateTime.now().plusMonths(6))
                .status(LotStatus.AVAILABLE)
                .build();

            lotDto = lotService.create(lotDto);
            System.out.println(lotDto);
    }
}
