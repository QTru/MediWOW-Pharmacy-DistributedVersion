package infrastructure.persistence.implementation;

import core.entities.Lot;
import infrastructure.persistence.LotRepository;

public class LotRepositoryImplementation extends AbstractGenericRepositoryImplementation<Lot, String> implements LotRepository {
    public LotRepositoryImplementation() {
        super(Lot.class);
    }
}
