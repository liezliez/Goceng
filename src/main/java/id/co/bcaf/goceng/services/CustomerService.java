package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.CustomerRequest;
import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.exceptions.ResourceNotFoundException;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Plafon;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.EmployeeRepository;
import id.co.bcaf.goceng.repositories.PlafonRepository;
import id.co.bcaf.goceng.repositories.UserRepository;
import id.co.bcaf.goceng.utils.NullAwareBeanUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlafonRepository plafonRepository;

    public CustomerResponse createCustomerFromUser(User user, String name, String nik) {
        if (employeeRepository.existsByUser_IdUser(user.getIdUser())) {
            throw new IllegalStateException("User is already registered as an employee");
        }

        Plafon lowestPlafon = plafonRepository.findLowestPlafon()
                .orElseThrow(() -> new RuntimeException("No plafon found"));

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(name);
        customer.setNik(nik);
        customer.setPlafon(lowestPlafon);

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (employeeRepository.existsByUser_IdUser(user.getIdUser())) {
            throw new IllegalStateException("User is already registered as an employee");
        }

        Plafon lowestPlafon = plafonRepository.findLowestPlafon()
                .orElseThrow(() -> new RuntimeException("No plafon found"));

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(request.getName());
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
        customer.setUrlKtp(request.getUrlKtp());
        customer.setUrlSelfie(request.getUrlSelfie());
        customer.setPlafon(lowestPlafon); // <- Assign plafon

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

    public Customer updateCustomer(UUID id, CustomerRequest request) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        existing.setName(request.getName());
        existing.setNik(request.getNik());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setPlaceOfBirth(request.getPlaceOfBirth());
        existing.setTelpNo(request.getTelpNo());
        existing.setAddress(request.getAddress());
        existing.setMotherMaidenName(request.getMotherMaidenName());
        existing.setOccupation(request.getOccupation());
        existing.setSalary(request.getSalary());
        existing.setHomeOwnershipStatus(request.getHomeOwnershipStatus());
        existing.setEmergencyCall(request.getEmergencyCall());
        existing.setCreditLimit(request.getCreditLimit());
        existing.setAccountNo(request.getAccountNo());
        existing.setUrlKtp(request.getUrlKtp());
        existing.setUrlSelfie(request.getUrlSelfie());

        return customerRepository.save(existing);
    }


    public CustomerResponse patchCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        NullAwareBeanUtils.copyNonNullProperties(request, customer);

        return mapToResponse(customerRepository.save(customer));
    }

    public Optional<Customer> findByUserId(UUID id) {
        return customerRepository.findByUser_IdUser(id);
    }

    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    public CustomerResponse convertToCustomerResponse(Customer customer) {
        return mapToResponse(customer);
    }


    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse res = new CustomerResponse();
        res.setIdCustomer(customer.getId());
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
        res.setName(customer.getName());
        res.setUrlKtp(customer.getUrlKtp());
        res.setUrlSelfie(customer.getUrlSelfie());
        if (customer.getPlafon() != null) {
            res.setPlafonId(customer.getPlafon().getId());
            res.setPlafonType(customer.getPlafon().getPlafonType());
            res.setPlafonAmount(customer.getPlafon().getPlafonAmount());
            res.setInterestRate(customer.getPlafon().getInterestRate());
        }
        return res;
    }

    public CustomerResponse updateKtpUrl(UUID customerId, String urlKtp) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setUrlKtp(urlKtp);
        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public CustomerResponse updateSelfieUrl(UUID customerId, String urlSelfie) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setUrlSelfie(urlSelfie);
        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

}
