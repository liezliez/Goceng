package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.enums.AccountStatus;
import id.co.bcaf.goceng.enums.WorkStatus;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.BranchRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;


    @Transactional
    public Employee createEmployee(UUID id_user, Integer id_role) {
        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (employeeRepository.existsByUser(user)) {
            throw new RuntimeException("Employee already exists for this user");
        }

        Role role = roleRepository.findById(id_role)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        if (user.getAccountStatus() == null) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setName(user.getName());

        // ✅ Convert String to UUID and fetch Branch
        UUID branchId = UUID.fromString("42F47C49-01B9-423A-AA56-D160F8196641");
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        employee.setBranch(branch);

        employee.setWorkStatus(WorkStatus.ACTIVE);
        employee.setNIP(generateRandomNIP());

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
                UUID branchId = UUID.fromString(request.getBranch()); // convert string to UUID
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new RuntimeException("Branch not found"));
                existingEmployee.setBranch(branch);  // set Branch object
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
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            throw new EntityNotFoundException("Employee not found with ID: " + id);
        }
        return employee.get();
    }



    private String generateRandomNIP() {
        return "NIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
