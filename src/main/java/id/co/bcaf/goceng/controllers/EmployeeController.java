package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.CreateEmployeeRequest;
import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.services.EmployeeService;
import id.co.bcaf.goceng.services.UserService;
import id.co.bcaf.goceng.utils.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Fetch employee by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<Employee> getEmployeeByUserId(@PathVariable UUID userId) {
        Optional<Employee> employee = employeeService.findByUserId(userId);
        return employee
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Create Employee using id_user and id_role from body
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody CreateEmployeeRequest request) {
        try {
            Employee employee = employeeService.createEmployee(
                    request.getId_user(),
                    request.getId_role()
            );
            return ResponseEntity.ok(employee);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Error creating employee: " + ex.getMessage());
        }
    }

    // ✅ Get All Employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // ✅ Get Employee by ID
    @GetMapping("/{id_employee}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable UUID id_employee) {
        Optional<Employee> employee = employeeService.getEmployeeById(id_employee);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ✅ Update Employee (only name, branch, workStatus)
    @PutMapping("/{id_employee}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable UUID id_employee,
            @RequestBody EmployeeUpdateRequest request
    ) {
        try {
            Optional<Employee> updatedEmployee = employeeService.updateEmployee(id_employee, request);
            return updatedEmployee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Error updating employee: " + ex.getMessage());
        }
    }

    // ✅ Soft Delete Employee
    @DeleteMapping("/{id_employee}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id_employee) {
        return employeeService.deleteEmployee(id_employee)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ✅ Restore Employee
    @PutMapping("/{id_employee}/restore")
    public ResponseEntity<Void> restoreEmployee(@PathVariable UUID id_employee) {
        boolean restored = employeeService.restoreEmployee(id_employee);
        return restored ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ✅ Handle Entity Not Found Exception
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
