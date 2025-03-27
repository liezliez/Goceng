package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
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
    private UserRepository userRepository;

    // ✅ Create Employee using id_user
    @Transactional
    public Employee createEmployee(UUID id_user) {
        // 🔹 Fetch user details using id_user
        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Create Employee with user details
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setName(user.getName());
        employee.setBranch("Default Branch");  // Set branch manually if needed
        employee.setWorkStatus("ACTIVE");      // Default status
        employee.setNIP(generateRandomNIP());  // Generate unique NIP

        return employeeRepository.save(employee);
    }

    // ✅ Get all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // ✅ Get employee by ID
    public Optional<Employee> getEmployeeById(UUID id_employee) {
        return employeeRepository.findById(id_employee);
    }

    // ✅ Update employee (Only updates fields, not user details)
    @Transactional
    public Optional<Employee> updateEmployee(UUID id_employee, Employee employeeDetails) {
        return employeeRepository.findByIdWithLock(id_employee).map(existingEmployee -> {
            existingEmployee.setBranch(employeeDetails.getBranch());
            existingEmployee.setWorkStatus(employeeDetails.getWorkStatus());
            return employeeRepository.save(existingEmployee);
        });
    }

    // ✅ Delete employee
    @Transactional
    public boolean deleteEmployee(UUID id_employee) {
        if (employeeRepository.existsById(id_employee)) {
            employeeRepository.deleteById(id_employee);
            return true;
        }
        return false;
    }

    // ✅ Helper function to generate unique NIP
    private String generateRandomNIP() {
        return "NIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
