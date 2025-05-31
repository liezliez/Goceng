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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('CREATE_USER')")
    public ResponseEntity<Employee> create(@RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeService.createEmployee(request.getId_user(), request.getId_role(), request.getBranchId());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USER')")
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USER')")
    public ResponseEntity<Employee> getById(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('VIEW_USER')")
    public ResponseEntity<Employee> getByUserId(@PathVariable UUID userId) {
        return employeeService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<Employee> update(@PathVariable UUID id, @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return employeeService.deleteEmployee(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("@rolePermissionEvaluator.hasRoleFeaturePermission('MANAGE_USERS')")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        return employeeService.restoreEmployee(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
