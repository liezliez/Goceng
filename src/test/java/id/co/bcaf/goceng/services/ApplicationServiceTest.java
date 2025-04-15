//package id.co.bcaf.goceng.services;
//
//import id.co.bcaf.goceng.enums.ApplicationStatus;
//import id.co.bcaf.goceng.models.Application;
//import id.co.bcaf.goceng.models.Customer;
//import id.co.bcaf.goceng.models.User;
//import id.co.bcaf.goceng.repositories.ApplicationRepository;
//import id.co.bcaf.goceng.repositories.CustomerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ApplicationServiceTest {
//
//    private ApplicationRepository applicationRepo;
//    private CustomerRepository customerRepo;
//    private ApplicationService applicationService;
//
//    @BeforeEach
//    void setup() {
//        applicationRepo = mock(ApplicationRepository.class);
//        customerRepo = mock(CustomerRepository.class);
//        applicationService = new ApplicationService(applicationRepo, customerRepo);
//    }
//
//    @Test
//    void testProgressStatus_BackOfficeApprovalApproved() {
//        // Setup mock data
//        UUID appId = UUID.randomUUID();
//        UUID customerId = UUID.randomUUID();
//
//        User customerUser = new User();
//        customerUser.setName("Test Customer");
//
//        Customer customer = new Customer();
//        customer.setIdCustomer(customerId);
//        customer.setUser(customerUser);
//
//        User backOfficeUser = new User();
//        backOfficeUser.setName("BackOffice Approver");
//
//        Application application = new Application();
//        application.setId(appId);
//        application.setCustomer(customer);
//        application.setAmount(BigDecimal.valueOf(1000000.5668));
//        application.setPurpose("Buy stuff");
//        application.setStatus(ApplicationStatus.PENDING_BACK_OFFICE);
//        application.setCreatedAt(LocalDateTime.now());
//        application.setUpdatedAt(LocalDateTime.now());
//
//        when(applicationRepo.findById(appId)).thenReturn(Optional.of(application));
//        when(applicationRepo.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Call the method
//        var response = applicationService.progressStatus(appId, true, backOfficeUser);
//
//        // Verify
//        assertNotNull(response);
//        assertEquals(ApplicationStatus.APPROVED.name(), response.getStatus());
//        assertEquals("Test Customer", response.getCustomerName());
//        assertEquals("BackOffice Approver", response.getBackOfficeApproverName());
//
//        verify(applicationRepo).save(any(Application.class));
//    }
//}
