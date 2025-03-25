package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
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

    // ✅ Create Employee
    @Transactional
    public Employee createEmployee(Employee employee) {
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

    // ✅ Update employee (Optimistic Locking Applied)
    @Transactional
    public Optional<Employee> updateEmployee(UUID id_employee, Employee employeeDetails) {
        return employeeRepository.findByIdWithLock(id_employee).map(existingEmployee -> {
            existingEmployee.setNIP(employeeDetails.getNIP());
            existingEmployee.setName(employeeDetails.getName());
            existingEmployee.setBranch(employeeDetails.getBranch());
            existingEmployee.setWorkStatus(employeeDetails.getWorkStatus());
            return employeeRepository.save(existingEmployee);  // ✅ Save only after updating fields
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
}
