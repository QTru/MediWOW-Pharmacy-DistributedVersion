package infrastructure.persistence.implementation;

import core.entities.Staff;
import infrastructure.persistence.StaffRepository;

public class StaffRepositoryImplementation extends AbstractGenericRepositoryImplementation<Staff, String> implements StaffRepository {
    public StaffRepositoryImplementation() {
        super(Staff.class);
    }

    @Override
    public Staff create(Staff staff) {
        staff.setPassword("firstPassword");
        return super.create(staff);
    }
}
