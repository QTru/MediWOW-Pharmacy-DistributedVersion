package infrastructure.persistence.implementation;

import core.entities.Measurement;
import core.entities.Product;
import core.entities.UnitOfMeasure;
import infrastructure.persistence.MeasurementRepository;
import infrastructure.persistence.ProductRepository;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImplementation extends AbstractGenericRepositoryImplementation<Product, String> implements ProductRepository {
    private final MeasurementRepository measurementRepository;

    public ProductRepositoryImplementation() {
        super(Product.class);
        measurementRepository = new MeasurementRepositoryImplementation();
    }

    @Override
    public Product create(Product product) {
        return doInTransaction(em -> {
            // Validate at least one UoM
            if (product.getUnitOfMeasures() == null || product.getUnitOfMeasures().isEmpty())
                throw new IllegalArgumentException("Product must have at least one unit of measure");

            // Validate exactly one base UoM
            long baseCount = product.getUnitOfMeasures().stream()
                    .filter(UnitOfMeasure::isBaseUnit)
                    .count();
            if (baseCount == 0)
                throw new IllegalArgumentException("Product must have exactly one base unit of measure");
            if (baseCount > 1)
                throw new IllegalArgumentException("Product can only have one base unit of measure");

            // Set product reference and resolve measurements for all UoMs
            product.getUnitOfMeasures().forEach(uom -> {
                uom.setProduct(product);
                resolveMeasurement(em, uom);
            });

            em.persist(product);
            return product;
        });
    }

    @Override
    public Product update(Product product) {
        return doInTransaction(em -> {
            Product existing = em.find(Product.class, product.getId());
            if (existing == null)
                throw new IllegalArgumentException("Product not found: " + product.getId());

            // Update basic fields
            existing.setName(product.getName());
            existing.setBarcode(product.getBarcode());
            existing.setCategory(product.getCategory());
            existing.setForm(product.getForm());
            existing.setShortName(product.getShortName());
            existing.setManufacturer(product.getManufacturer());
            existing.setIngredients(product.getIngredients());
            existing.setVat(product.getVat());
            existing.setStrength(product.getStrength());
            existing.setDescription(product.getDescription());

            if (product.getUnitOfMeasures() != null) {
                // Find existing base UoM — must be preserved
                UnitOfMeasure existingBaseUom = existing.getUnitOfMeasures().stream()
                        .filter(UnitOfMeasure::isBaseUnit)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Existing product has no base unit of measure"));

                // Ensure incoming list still contains the same base UoM
                boolean basePreserved = product.getUnitOfMeasures().stream()
                        .anyMatch(uom -> uom.isBaseUnit() && uom.getMeasurement().getName()
                                .equals(existingBaseUom.getMeasurement().getName()));
                if (!basePreserved)
                    throw new IllegalArgumentException("Cannot remove or change the base unit of measure");

                // Resolve measurements for non-base UoMs only
                List<UnitOfMeasure> newNonBaseUoms = product.getUnitOfMeasures().stream()
                        .filter(uom -> !uom.isBaseUnit())
                        .toList();

                newNonBaseUoms.forEach(uom -> {
                    uom.setProduct(existing);
                    resolveMeasurement(em, uom);
                });

                // orphanRemoval handles deletion of removed UoMs
                existing.getUnitOfMeasures().clear();
                existing.getUnitOfMeasures().add(existingBaseUom);     // preserve base
                existing.getUnitOfMeasures().addAll(newNonBaseUoms);   // add updated non-base
            }

            return existing;
        });
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private void resolveMeasurement(EntityManager em, UnitOfMeasure uom) {
        if (uom.getMeasurement() == null)
            throw new IllegalArgumentException("UnitOfMeasure must have a measurement");
        Measurement existing = (Measurement) measurementRepository
                .findByName(uom.getMeasurement().getName());
        if (existing != null)
            uom.setMeasurement(em.merge(existing));
        // null → new Measurement, cascade from UoM will persist it
    }

    public static void main(String[] args) {
        ProductRepository productRepository = new ProductRepositoryImplementation();

//        Product product = Product.builder()
//                .name("Paracetamol 500mg")
//                .barcode("1234567891")
//                .unitOfMeasures(List.of(
//                        UnitOfMeasure.builder()
//                                .measurement(Measurement.builder().name("Vien").build())
//                                .baseUnit(true)
//                                .baseUnitConversionRate(BigDecimal.ONE)
//                                .build()
//                        ,UnitOfMeasure.builder()
//                                .measurement(Measurement.builder().name("Hop").build())
//                                .baseUnit(false)
//                                .baseUnitConversionRate(BigDecimal.valueOf(0.5))
//                                .build()
//                ))
//                .build();
//
//        productRepository.create(product);

        Product product = productRepository.findById("PRO000007");
        product.setUnitOfMeasures(List.of(
                UnitOfMeasure.builder()
                        .measurement(Measurement.builder().name("Hop").build())
                        .baseUnit(false)
                        .baseUnitConversionRate(BigDecimal.valueOf(10))
                        .build()
                ,UnitOfMeasure.builder()
                        .measurement(Measurement.builder().name("Ong").build())
                        .baseUnit(true)
                        .baseUnitConversionRate(BigDecimal.ONE)
                        .build()
        ));

        productRepository.update(product);
    }
}
