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
    public Staff create(Staff staff) {
        // Nếu GUI gửi lên password plain-text thì hash nó.
        // Nếu password null (không được set từ DTO) thì dùng mật khẩu mặc định.
        String raw = staff.getPassword();
        if (raw == null || raw.isBlank()) {
            raw = "firstPassword";
        }
        staff.setPassword(PasswordUtil.hashPassword(raw));
        return super.create(staff);
    }
    public Staff update(Staff staff) {
        // Khi sửa nhân viên, GUI không gửi password → staff.password = null.
        // Load password cũ từ DB để tránh mất password.
        if (staff.getPassword() == null || staff.getPassword().isBlank()) {
            Staff existing = findById(staff.getId());
            if (existing != null) {
                staff.setPassword(existing.getPassword());
            }
        }
        return super.update(staff);
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

