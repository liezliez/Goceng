package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
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

    // # Create Employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.createEmployee(employee));
    }

    // # Get All Employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // # Get Employee by ID
    @GetMapping("/{id_employee}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable UUID id_employee) {
        Optional<Employee> employee = employeeService.getEmployeeById(id_employee);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // # Update Employee
    @PutMapping("/{id_employee}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable UUID id_employee, @RequestBody Employee employeeDetails) {
        Optional<Employee> updatedEmployee = employeeService.updateEmployee(id_employee, employeeDetails);
        return updatedEmployee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // # Delete Employee
    @DeleteMapping("/{id_employee}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id_employee) {
        return employeeService.deleteEmployee(id_employee) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
