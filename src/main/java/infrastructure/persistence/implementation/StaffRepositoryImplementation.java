package infrastructure.persistence.implementation;

import core.entities.Staff;
import infrastructure.persistence.StaffRepository;

public class StaffRepositoryImplementation extends AbstractGenericRepositoryImplementation<Staff, String> implements StaffRepository {
    public StaffRepositoryImplementation() {
        super(Staff.class);
    }
}
