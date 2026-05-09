package infrastructure.service.implementation;

import core.dto.StaffDto;
import core.entities.Staff;
import core.entities.enums.Role;
import core.utils.PasswordUtil;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.StaffRepository;
import infrastructure.persistence.implementation.StaffRepositoryImplementation;
import infrastructure.service.StaffService;

import java.util.List;

public class StaffServiceImplementation implements StaffService {
    private final StaffRepository staffRepository;

    public StaffServiceImplementation() {
        staffRepository = new StaffRepositoryImplementation();
    }

    @Override
    public StaffDto login(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống");

        Staff staff = staffRepository.findByUsername(username);
        if (staff == null)
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
        if (!PasswordUtil.verifyPassword(password, staff.getPassword()))
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
        if (!staff.isActive())
            throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản lý.");

        return Mapper.map(staff, StaffDto.class);
    }

    @Override
    public StaffDto create(StaffDto staffDto) {
        checkGeneralInfo(staffDto);

        Staff staff = Mapper.map(staffDto, Staff.class);
        staff = staffRepository.create(staff);
        return Mapper.map(staff, StaffDto.class);
    }

    @Override
    public StaffDto update(StaffDto staffDto) {
        if (staffDto.getId() == null || staffDto.getId().isBlank())
            throw new IllegalArgumentException("Staff id cannot be null or blank");
        checkGeneralInfo(staffDto);

        Staff staff = Mapper.map(staffDto, Staff.class);
        staff = staffRepository.update(staff);
        return Mapper.map(staff, StaffDto.class);
    }

    @Override
    public StaffDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Staff staff = staffRepository.findById(id);
        if (staff == null)
            throw new IllegalArgumentException("Staff with id " + id + " not found");

        return Mapper.map(staff, StaffDto.class);
    }

    @Override
    public List<StaffDto> loadAll() {
        return staffRepository.loadAll()
                .stream()
                .map(s -> Mapper.map(s, StaffDto.class))
                .toList();
    }

    private void checkGeneralInfo(StaffDto staffDto) {
        if (staffDto.getUsername() == null || staffDto.getUsername().isBlank())
            throw new IllegalArgumentException("Username cannot be null or blank");
        if (staffDto.getFullName() == null || staffDto.getFullName().isBlank())
            throw new IllegalArgumentException("Full name cannot be null or blank");
        if (staffDto.getLicenseNumber() == null || staffDto.getLicenseNumber().isBlank())
            throw new IllegalArgumentException("License number cannot be null or blank");
        if (staffDto.getPhoneNumber() == null || staffDto.getPhoneNumber().isBlank())
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        if (staffDto.getEmail() == null || staffDto.getEmail().isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");
        if (staffDto.getRole() == null)
            throw new IllegalArgumentException("Role cannot be null");
    }

    public static void main(String[] args) {
        StaffService staffService = new StaffServiceImplementation();

        StaffDto staffDto = StaffDto
                .builder()
                .username("john_doe")
                .fullName("John Doe")
                .licenseNumber("123456789")
                .phoneNumber("555-1234")
                .email("something@gmail.com")
                .role(Role.PHARMACIST)
                .build();

        staffDto = staffService.create(staffDto);
        System.out.println(staffDto);
    }
}