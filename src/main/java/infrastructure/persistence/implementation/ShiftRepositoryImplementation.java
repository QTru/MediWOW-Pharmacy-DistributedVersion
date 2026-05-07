package infrastructure.persistence.implementation;

import core.entities.Shift;
import core.entities.Staff;
import infrastructure.persistence.ShiftRepository;

public class ShiftRepositoryImplementation extends AbstractGenericRepositoryImplementation<Shift, String> implements ShiftRepository {
    public ShiftRepositoryImplementation() {
        super(Shift.class);
    }

    @Override
    public Shift create(Shift shift) {
        return doInTransaction(em -> {
            if (shift.getStaff() == null)
                throw new IllegalArgumentException("Shift must have a creator");
            shift.setStaff(em.getReference(Staff.class, shift.getStaff().getId()));

            if (shift.getClosedByStaff() != null)
                shift.setClosedByStaff(em.getReference(Staff.class, shift.getClosedByStaff().getId()));

            em.persist(shift);
            return shift;
        });
    }

    @Override
    public Shift update(Shift shift) {
        return doInTransaction(em -> {
            if (shift.getStaff() != null)
                shift.setStaff(em.getReference(Staff.class, shift.getStaff().getId()));

            if (shift.getClosedByStaff() != null)
                shift.setClosedByStaff(em.getReference(Staff.class, shift.getClosedByStaff().getId()));

            em.merge(shift);
            return shift;
        });
    }
}
