package infrastructure.persistence.implementation;

import core.entities.Staff;
import core.utils.PasswordUtil;
import infrastructure.persistence.StaffRepository;

import java.util.List;

public class StaffRepositoryImplementation
        extends AbstractGenericRepositoryImplementation<Staff, String>
        implements StaffRepository {

    public StaffRepositoryImplementation() {
        super(Staff.class);
    }

    // ─── Override: hash password before persisting ────────────────────────────
    @Override
    public Staff create(Staff staff) {
        staff.setPassword(PasswordUtil.hashPassword("firstPassword"));
        return super.create(staff);
    }

    // ─── Query by username ────────────────────────────────────────────────────
    @Override
    public Staff findByUsername(String username) {
        return doInTransaction(em -> {
            List<Staff> results = em
                    .createQuery("FROM Staff s WHERE s.username = :username", Staff.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        });
    }
}

