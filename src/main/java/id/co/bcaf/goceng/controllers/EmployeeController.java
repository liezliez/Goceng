package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.models.Employee;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.services.EmployeeService;
import id.co.bcaf.goceng.services.UserService;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    // ✅ Create Employee using Authenticated User's id_user
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestHeader("Authorization") String authHeader) {
        // 🔹 Extract email from JWT Token
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        // 🔹 Find User by email
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 🔹 Use `id_user` to create Employee
        Employee employee = employeeService.createEmployee(user.get().getId_user());
        return ResponseEntity.ok(employee);
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

    // ✅ Update Employee
    @PutMapping("/{id_employee}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable UUID id_employee, @RequestBody Employee employeeDetails) {
        Optional<Employee> updatedEmployee = employeeService.updateEmployee(id_employee, employeeDetails);
        return updatedEmployee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ✅ Delete Employee
    @DeleteMapping("/{id_employee}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id_employee) {
        return employeeService.deleteEmployee(id_employee) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
