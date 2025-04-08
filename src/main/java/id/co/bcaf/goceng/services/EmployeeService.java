package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.enums.AccountStatus;
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
        employee.setBranch("Default Branch");
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

    // ✅ Updated to remove id_user; sync user.name from employee.name
    @Transactional
    public Optional<Employee> updateEmployee(UUID id_employee, EmployeeUpdateRequest request) {
        return employeeRepository.findByIdWithLock(id_employee).map(existingEmployee -> {
            if (request.getName() != null) {
                existingEmployee.setName(request.getName());
            }
            if (request.getBranch() != null) {
                existingEmployee.setBranch(request.getBranch());
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

    private String generateRandomNIP() {
        return "NIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
