package infrastructure.persistence.implementation;

import core.entities.Shift;
import core.entities.Staff;
import core.entities.enums.ShiftStatus;
import infrastructure.persistence.ShiftRepository;

import java.time.LocalDateTime;

public class ShiftRepositoryImplementation extends AbstractGenericRepositoryImplementation<Shift, String> implements ShiftRepository {
    public ShiftRepositoryImplementation() {
        super(Shift.class);
    }

    @Override
    public Shift create(Shift shift) {
        return doInTransaction(em -> {
            shift.setStaff(em.find(Staff.class, shift.getStaff().getId()));
            shift.setStartTime(LocalDateTime.now());
            shift.setStatus(ShiftStatus.OPEN);

            em.persist(shift);
            return shift;
        });
    }

    @Override
    public Shift update(Shift shift) {
        return doInTransaction(em -> {
            shift.setClosedByStaff(em.find(Staff.class, shift.getClosedByStaff().getId()));
            shift.setEndTime(LocalDateTime.now());
            shift.setStatus(ShiftStatus.CLOSED);

            em.merge(shift);
            return shift;
        });
    }
}
