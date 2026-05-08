package infrastructure.service.implementation;

import core.dto.MeasurementDto;
import core.dto.ProductDto;
import core.dto.UnitOfMeasureDto;
import core.entities.Product;
import core.entities.enums.DosageForm;
import core.entities.enums.ProductCategory;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.ProductRepository;
import infrastructure.persistence.implementation.ProductRepositoryImplementation;
import infrastructure.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

public class ProductServiceImplementation implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImplementation() {
        productRepository = new ProductRepositoryImplementation();
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        checkGeneralInfo(productDto);

        Product product = Mapper.map(productDto);
        product = productRepository.create(product);
        return Mapper.map(product);
    }

    @Override
    public ProductDto update(ProductDto productDto) {
        if (productDto.getId() == null || productDto.getId().isBlank())
            throw new IllegalArgumentException("Product id cannot be null or blank");

        checkGeneralInfo(productDto);

        Product product = Mapper.map(productDto);
        product = productRepository.update(product);
        return Mapper.map(product);
    }

    @Override
    public ProductDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Product product = productRepository.findById(id);
        if (product == null)
            throw new IllegalArgumentException("Product with id " + id + " not found");

        return Mapper.map(product);
    }

    @Override
    public List<ProductDto> loadAll() {
        return productRepository.loadAll()
                .stream()
                .map(Mapper::map)
                .toList();
    }

    private void checkGeneralInfo(ProductDto productDto) {
        if (productDto.getBarcode() == null || productDto.getBarcode().isBlank())
            throw new IllegalArgumentException("Product barcode cannot be null or blank");
        if (productDto.getCategory() == null)
            throw new IllegalArgumentException("Product category cannot be null");
        if (productDto.getForm() == null)
            throw new IllegalArgumentException("Product form cannot be null");
        if (productDto.getName() == null || productDto.getName().isBlank())
            throw new IllegalArgumentException("Product name cannot be null or blank");
        if (productDto.getShortName() == null || productDto.getShortName().isBlank())
            throw new IllegalArgumentException("Product short name cannot be null or blank");
        if (productDto.getManufacturer() == null || productDto.getManufacturer().isBlank())
            throw new IllegalArgumentException("Product manufacturer cannot be null or blank");
        if (productDto.getIngredients() == null || productDto.getIngredients().isBlank())
            throw new IllegalArgumentException("Product ingredients cannot be null or blank");
        if (productDto.getVat() == null || productDto.getVat().signum() < 0)
            throw new IllegalArgumentException("Product VAT cannot be null or negative");
        if (productDto.getUnitOfMeasures() == null || productDto.getUnitOfMeasures().isEmpty())
            throw new IllegalArgumentException("Product must have at least one unit of measure");

        long baseCount = productDto.getUnitOfMeasures().stream()
                .filter(UnitOfMeasureDto::isBaseUnit)
                .count();
        if (baseCount != 1)
            throw new IllegalArgumentException("Product must have exactly one base unit of measure");

        productDto.getUnitOfMeasures().forEach(uom -> {
            if (uom.getMeasurement() == null || uom.getMeasurement().getName() == null
                    || uom.getMeasurement().getName().isBlank())
                throw new IllegalArgumentException("Each unit of measure must have a measurement name");
            if (uom.getBaseUnitConversionRate() == null || uom.getBaseUnitConversionRate().signum() <= 0)
                throw new IllegalArgumentException("Each unit of measure must have a positive base unit conversion rate");
        });
    }

    public static void main(String[] args) {
        ProductService productService = new ProductServiceImplementation();

//        ProductDto productDto = ProductDto.builder()
//            .barcode("1234567890123")
//            .category(ProductCategory.OTC)
//            .form(DosageForm.SOLID_DOSAGE)
//            .name("Paracetamol 500mg")
//            .shortName("Paracetamol")
//            .manufacturer("Pharma Inc.")
//            .ingredients("Paracetamol")
//            .vat(BigDecimal.valueOf(0.1))
//            .unitOfMeasures(List.of(
//                UnitOfMeasureDto.builder()
//                    .measurement(MeasurementDto.builder().name("piece").build())
//                    .baseUnitConversionRate(BigDecimal.ONE)
//                    .baseUnit(true)
//                    .build(),
//                UnitOfMeasureDto.builder()
//                    .measurement(MeasurementDto.builder().name("box").build())
//                    .baseUnitConversionRate(BigDecimal.valueOf(10))
//                    .build()
//            ))
//            .build();
//
//        ProductDto created = productService.create(productDto);
//        System.out.println("Created product: " + created);

        ProductDto found = productService.findById("PRO000007");

        found.setDescription("Effective pain reliever");
        found.setUnitOfMeasures(List.of(
            UnitOfMeasureDto.builder()
                .measurement(MeasurementDto.builder().name("piece").build())
                .baseUnitConversionRate(BigDecimal.ONE)
                .baseUnit(true)
                .build(),
            UnitOfMeasureDto.builder()
                .measurement(MeasurementDto.builder().name("box").build())
                .baseUnitConversionRate(BigDecimal.valueOf(20))  // updated conversion rate
                .build(),
            UnitOfMeasureDto.builder()  // new unit of measure
                .measurement(MeasurementDto.builder().name("carton").build())
                .baseUnitConversionRate(BigDecimal.valueOf(200))
                .build()
        ));
        ProductDto updated = productService.update(found);
        System.out.println("Updated product: " + updated);
    }
}