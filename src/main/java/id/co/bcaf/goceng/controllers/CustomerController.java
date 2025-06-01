package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.CustomerRequest;
import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.services.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Customer entities.
 *
 * Provides endpoints to:
 * - Create a new customer ({@link #create})
 * - Retrieve all customers ({@link #getAll})
 * - Retrieve a customer by user ID ({@link #getCustomerByUserId})
 * - Retrieve a customer by ID ({@link #getById})
 * - Update a customer fully ({@link #update})
 * - Partially update a customer ({@link #patchCustomer})
 * - Delete a customer ({@link #delete})
 */

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // Constructor Injection of CustomerService
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            // Return 400 Bad Request if name is missing or empty
            return ResponseEntity.badRequest().build();
        }
        CustomerResponse createdCustomer = customerService.createCustomer(request);
        return ResponseEntity.ok(createdCustomer);
    }


    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable UUID userId) {
        Optional<Customer> customerOpt = customerService.findByUserId(userId);
        if (customerOpt.isPresent()) {
            CustomerResponse response = customerService.convertToCustomerResponse(customerOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable UUID id, @RequestBody CustomerRequest request) {
        Customer updatedCustomer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> patchCustomer(@PathVariable UUID id, @RequestBody CustomerRequest request) {
        CustomerResponse patchedCustomer = customerService.patchCustomer(id, request);
        return ResponseEntity.ok(patchedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
