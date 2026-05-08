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

                List<UnitOfMeasure> incomingNonBaseUoms = product.getUnitOfMeasures().stream()
                        .filter(uom -> !uom.isBaseUnit())
                        .toList();

                // Resolve measurements first so we can match by name
                incomingNonBaseUoms.forEach(uom -> resolveMeasurement(em, uom));

                // Snapshot of existing non-base UoMs — track which ones were "handled"
                List<UnitOfMeasure> existingNonBaseUoms = new ArrayList<>(
                        existing.getUnitOfMeasures().stream()
                                .filter(uom -> !uom.isBaseUnit())
                                .toList());

                for (UnitOfMeasure incoming : incomingNonBaseUoms) {
                    String incomingMeasurementName = incoming.getMeasurement().getName();

                    UnitOfMeasure match = existingNonBaseUoms.stream()
                            .filter(e -> e.getMeasurement().getName().equals(incomingMeasurementName))
                            .findFirst()
                            .orElse(null);

                    if (match != null) {
                        // ✅ Same measurement already exists — update fields IN-PLACE,
                        //    no identity conflict because we never removed it
                        match.setPrice(incoming.getPrice());
                        match.setBaseUnitConversionRate(incoming.getBaseUnitConversionRate());
                        existingNonBaseUoms.remove(match); // mark as handled
                    } else {
                        // Truly new measurement — safe to add
                        incoming.setProduct(existing);
                        existing.getUnitOfMeasures().add(incoming);
                    }
                }

                // Whatever remains in existingNonBaseUoms was not in the incoming list → remove
                existing.getUnitOfMeasures().removeAll(existingNonBaseUoms);
            }

            return existing;
        });
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private void resolveMeasurement(EntityManager em, UnitOfMeasure uom) {
        if (uom.getMeasurement() == null)
            throw new IllegalArgumentException("UnitOfMeasure must have a measurement");
        Measurement existing = measurementRepository
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
                        .measurement(Measurement.builder().name("piece").build())
                        .baseUnit(true)
                        .baseUnitConversionRate(BigDecimal.ONE)
                        .build()
                ,UnitOfMeasure.builder()
                        .measurement(Measurement.builder().name("box").build())
                        .baseUnit(false)
                        .baseUnitConversionRate(BigDecimal.valueOf(20))
                        .build()
        ));

        productRepository.update(product);
    }
}
