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


    public Optional<Employee> findByUserId(UUID userId) {
        return employeeRepository.findByUser_IdUser(userId);
    }

    @Transactional
    public Employee createEmployee(UUID id_user, Integer id_role) {
        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id_user));

        if (customerRepository.existsByUser_IdUser(id_user)) {
            throw new IllegalStateException("User is already registered as a customer");
        }

        if (employeeRepository.existsByUser_IdUser(id_user)) {
            throw new IllegalStateException("Employee already exists for this user");
        }

        Role role = roleRepository.findById(id_role)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id_role));
        user.setRole(role);

        if (user.getAccountStatus() == null) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setName(user.getName());

        UUID branchId = UUID.fromString("42F47C49-01B9-423A-AA56-D160F8196641");
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with ID: " + branchId));
        employee.setBranch(branch);

        employee.setWorkStatus(WorkStatus.ACTIVE);
        employee.setNIP(generateNIP(role));

        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(UUID id_employee) {
        return employeeRepository.findById(id_employee);
    }

    @Transactional
    public Optional<Employee> updateEmployee(UUID id_employee, EmployeeUpdateRequest request) {
        return employeeRepository.findByIdWithLock(id_employee).map(existingEmployee -> {
            if (request.getName() != null) {
                existingEmployee.setName(request.getName());
            }

            if (request.getBranch() != null) {
                UUID branchId = UUID.fromString(request.getBranch());
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new EntityNotFoundException("Branch not found with ID: " + branchId));
                existingEmployee.setBranch(branch);
            }

            if (request.getWorkStatus() != null) {
                existingEmployee.setWorkStatus(request.getWorkStatus());
            }

            return employeeRepository.save(existingEmployee);
        });
    }

    @Transactional
    public boolean deleteEmployee(UUID id_employee) {
        return employeeRepository.findByIdWithLock(id_employee).map(employee -> {
            employee.setWorkStatus(WorkStatus.INACTIVE);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean restoreEmployee(UUID id_employee) {
        return employeeRepository.findByIdWithLock(id_employee).map(employee -> {
            employee.setWorkStatus(WorkStatus.ACTIVE);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    public Employee findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));
    }

    private String generateNIP(Role role) {
        String year = String.valueOf(java.time.Year.now().getValue());
        String roleCode = getRoleCode(role.getRoleName()); // example: ROLE_MARKETING -> MK
        String prefix = year + roleCode;

        Optional<Employee> lastEmployee = employeeRepository.findTopByNIPStartingWithOrderByNIPDesc(prefix);
        int nextNumber = lastEmployee.map(e -> {
            String nip = e.getNIP();
            String numberPart = nip.substring(prefix.length());
            return Integer.parseInt(numberPart) + 1;
        }).orElse(1);

        return String.format("%s%03d", prefix, nextNumber);
    }

    private String getRoleCode(String roleName) {
        return switch (roleName) {
            case "ROLE_MARKETING" -> "MK";
            case "ROLE_BRANCH_MANAGER" -> "BM";
            case "ROLE_BACK_OFFICE" -> "BO";
            case "ROLE_SUPERADMIN" -> "SA";
            default -> "OT"; // Other
        };
    }

}
