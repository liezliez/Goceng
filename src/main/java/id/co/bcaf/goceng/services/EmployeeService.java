package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.enums.WorkStatus;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;

    private static final UUID DEFAULT_BRANCH_ID = UUID.fromString("B43A94D7-4C5E-4F2D-8A7B-02477F36D65F");

    public Optional<Employee> findByUserId(UUID userId) {
        return employeeRepository.findByUser_IdUser(userId);
    }

    @Transactional
    public Employee createEmployee(UUID idUser, Integer idRole, UUID branchId) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + idUser));

        if (customerRepository.existsByUser_IdUser(idUser))
            throw new IllegalStateException("User is already a customer");

        if (employeeRepository.existsByUser_IdUser(idUser))
            throw new IllegalStateException("Employee already exists for user");

        Role role = roleRepository.findById(idRole)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + idRole));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found: " + branchId));

        user.setRole(role);
        if (user.getAccountStatus() == null) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setName(user.getName());
        employee.setBranch(branch);
        employee.setWorkStatus(WorkStatus.ACTIVE);
        employee.setNIP(generateNIP(role));

        Employee savedEmployee = employeeRepository.save(employee);

        user.setEmployee(savedEmployee);
        userRepository.save(user);

        return savedEmployee;
    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(UUID idEmployee) {
        return employeeRepository.findById(idEmployee);
    }

    @Transactional
    public Optional<Employee> updateEmployee(UUID idEmployee, EmployeeUpdateRequest request) {
        return employeeRepository.findByIdWithLock(idEmployee).map(employee -> {
            Optional.ofNullable(request.getName()).ifPresent(employee::setName);

            if (request.getBranch() != null) {
                UUID branchId = UUID.fromString(request.getBranch());
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new EntityNotFoundException("Branch not found: " + branchId));
                employee.setBranch(branch);
            }

            Optional.ofNullable(request.getWorkStatus()).ifPresent(employee::setWorkStatus);

            return employeeRepository.save(employee);
        });
    }

    @Transactional
    public boolean deleteEmployee(UUID idEmployee) {
        return employeeRepository.findByIdWithLock(idEmployee).map(employee -> {
            employee.setWorkStatus(WorkStatus.INACTIVE);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean restoreEmployee(UUID idEmployee) {
        return employeeRepository.findByIdWithLock(idEmployee).map(employee -> {
            employee.setWorkStatus(WorkStatus.ACTIVE);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    public Employee findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    private String generateNIP(Role role) {
        String year = String.valueOf(Year.now().getValue());
        String roleCode = getRoleCode(role.getRoleName());
        String prefix = year + roleCode;

        int nextSequence = employeeRepository.findTopByNIPStartingWithOrderByNIPDesc(prefix)
                .map(e -> {
                    String numberPart = e.getNIP().substring(prefix.length());
                    return Integer.parseInt(numberPart) + 1;
                }).orElse(1);

        return String.format("%s%03d", prefix, nextSequence);
    }

    private String getRoleCode(String roleName) {
        return switch (roleName) {
            case "ROLE_MARKETING" -> "MK";
            case "ROLE_BRANCH_MANAGER" -> "BM";
            case "ROLE_BACK_OFFICE" -> "BO";
            case "ROLE_SUPERADMIN" -> "SA";
            default -> "OT";
        };
    }
}
