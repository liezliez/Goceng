package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.enums.WorkStatus;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.RoleRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
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

    // ✅ Create an Employee using id_user (UUID from body) and assign role by id_role
    @Transactional
    public Employee createEmployee(UUID id_user, Integer id_role) {
        // 1. Find user
        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if employee already exists
        if (employeeRepository.existsByUser(user)) {
            throw new RuntimeException("Employee already exists for this user");
        }

        // 3. Set user's role
        Role role = roleRepository.findById(id_role)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);

        // 4. Create employee from user data
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setName(user.getName());
        employee.setBranch("Default Branch");
        employee.setWorkStatus(WorkStatus.ACTIVE);
        employee.setNIP(generateRandomNIP());

        return employeeRepository.save(employee);
    }

    // ✅ Fetch all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // ✅ Get employee by ID
    public Optional<Employee> getEmployeeById(UUID id_employee) {
        return employeeRepository.findById(id_employee);
    }

    // ✅ Update employee (only name and branch)
    @Transactional
    public Optional<Employee> updateEmployee(UUID id_employee, EmployeeUpdateRequest request) {
        return employeeRepository.findByIdWithLock(id_employee).map(existingEmployee -> {
            if (request.getName() != null) {
                existingEmployee.setName(request.getName());
            }
            if (request.getBranch() != null) {
                existingEmployee.setBranch(request.getBranch());
            }
            return employeeRepository.save(existingEmployee);
        });
    }

    // ✅ Soft delete employee by setting workStatus to INACTIVE
    @Transactional
    public boolean deleteEmployee(UUID id_employee) {
        return employeeRepository.findByIdWithLock(id_employee).map(employee -> {
            employee.setWorkStatus(WorkStatus.INACTIVE);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    // ✅ Generate a unique NIP for employee
    private String generateRandomNIP() {
        return "NIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
