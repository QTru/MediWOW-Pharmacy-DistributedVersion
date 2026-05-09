package infrastructure.persistence;

import core.entities.Staff;

public interface StaffRepository extends GenericRepository<Staff, String> {
    Staff findByUsername(String username);
}
