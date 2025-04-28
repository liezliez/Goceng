package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.CreateEmployeeRequest;
import id.co.bcaf.goceng.dto.EmployeeUpdateRequest;
import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.services.EmployeeService;
import id.co.bcaf.goceng.services.UserService;
import id.co.bcaf.goceng.utils.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Employee> getEmployeeByUserId(@PathVariable UUID userId) {
        return employeeService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeService.createEmployee(request.getId_user(), request.getId_role());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id_employee}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable UUID id_employee) {
        return employeeService.getEmployeeById(id_employee)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id_employee}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable UUID id_employee,
            @RequestBody EmployeeUpdateRequest request
    ) {
        return employeeService.updateEmployee(id_employee, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id_employee}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id_employee) {
        return employeeService.deleteEmployee(id_employee)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id_employee}/restore")
    public ResponseEntity<Void> restoreEmployee(@PathVariable UUID id_employee) {
        return employeeService.restoreEmployee(id_employee)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
