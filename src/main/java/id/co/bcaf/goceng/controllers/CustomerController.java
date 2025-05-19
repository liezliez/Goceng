package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.dto.CustomerRequest;
import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.services.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // Constructor Injection of CustomerService
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Create a new Customer.
     * Requires 'name' field in request, otherwise returns 400 Bad Request.
     * @param request CustomerRequest containing customer details
     * @return Created CustomerResponse
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            // Return 400 Bad Request if name is missing or empty
            return ResponseEntity.badRequest().build();
        }
        CustomerResponse createdCustomer = customerService.createCustomer(request);
        return ResponseEntity.ok(createdCustomer);
    }

    /**
     * Retrieve all customers.
     * @return List of CustomerResponse
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * Retrieve a specific customer by ID.
     * @param id UUID of the customer
     * @return CustomerResponse of requested customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Update an existing customer with full replacement.
     * Expects all customer fields in request.
     * @param id UUID of the customer to update
     * @param request CustomerRequest with updated data
     * @return Updated CustomerResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @RequestBody CustomerRequest request) {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * Partially update an existing customer.
     * Only non-null fields in request will be updated.
     * @param id UUID of the customer to patch
     * @param request CustomerRequest with fields to update
     * @return Patched CustomerResponse
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> patchCustomer(@PathVariable UUID id, @RequestBody CustomerRequest request) {
        CustomerResponse patchedCustomer = customerService.patchCustomer(id, request);
        return ResponseEntity.ok(patchedCustomer);
    }

    /**
     * Delete a customer by ID.
     * @param id UUID of the customer to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
