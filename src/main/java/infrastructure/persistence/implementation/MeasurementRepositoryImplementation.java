package infrastructure.persistence.implementation;

import core.entities.Measurement;
import infrastructure.persistence.MeasurementRepository;

import java.util.List;

public class MeasurementRepositoryImplementation extends AbstractGenericRepositoryImplementation<Measurement, String> implements MeasurementRepository {
    public MeasurementRepositoryImplementation() {
        super(Measurement.class);
    }

    @Override
    public Object findByName(String name) {
        String query = "FROM Measurement m WHERE m.name = :name";

        return doInTransaction(em -> {
            try {
                return em.createQuery(query)
                        .setParameter("name", name)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        });
    }
}
