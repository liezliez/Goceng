package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.CustomerRequest;
import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (employeeRepository.existsByUser_IdUser(user.getIdUser())) {
            throw new IllegalStateException("User is already registered as an employee");
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setNik(request.getNik());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPlaceOfBirth(request.getPlaceOfBirth());
        customer.setTelpNo(request.getTelpNo());
        customer.setAddress(request.getAddress());
        customer.setMotherMaidenName(request.getMotherMaidenName());
        customer.setOccupation(request.getOccupation());
        customer.setSalary(request.getSalary());
        customer.setHomeOwnershipStatus(request.getHomeOwnershipStatus());
        customer.setEmergencyCall(request.getEmergencyCall());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setAccountNo(request.getAccountNo());

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }


    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }

    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setNik(request.getNik());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPlaceOfBirth(request.getPlaceOfBirth());
        customer.setTelpNo(request.getTelpNo());
        customer.setAddress(request.getAddress());
        customer.setMotherMaidenName(request.getMotherMaidenName());
        customer.setOccupation(request.getOccupation());
        customer.setSalary(request.getSalary());
        customer.setHomeOwnershipStatus(request.getHomeOwnershipStatus());
        customer.setEmergencyCall(request.getEmergencyCall());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setAccountNo(request.getAccountNo());

        return mapToResponse(customerRepository.save(customer));
    }

    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse res = new CustomerResponse();
        res.setIdCustomer(customer.getIdCustomer());
        res.setIdUser(customer.getUser().getIdUser());
        res.setNik(customer.getNik());
        res.setDateOfBirth(customer.getDateOfBirth());
        res.setPlaceOfBirth(customer.getPlaceOfBirth());
        res.setTelpNo(customer.getTelpNo());
        res.setAddress(customer.getAddress());
        res.setMotherMaidenName(customer.getMotherMaidenName());
        res.setOccupation(customer.getOccupation());
        res.setSalary(customer.getSalary());
        res.setHomeOwnershipStatus(customer.getHomeOwnershipStatus());
        res.setEmergencyCall(customer.getEmergencyCall());
        res.setCreditLimit(customer.getCreditLimit());
        res.setAccountNo(customer.getAccountNo());
        return res;
    }
}
