package infrastructure.persistence;

import core.entities.Measurement;

public interface MeasurementRepository extends GenericRepository<Measurement, String> {
    Measurement findByName(String name);
}
