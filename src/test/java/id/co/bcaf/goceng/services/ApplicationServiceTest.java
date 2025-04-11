package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.User;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepo;

    @Mock
    private CustomerRepository customerRepo;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProgressStatus_MarketingApprovalApproved() {
        // Arrange
        UUID appId = UUID.randomUUID();

        User marketingUser = new User();
        marketingUser.setIdUser(UUID.randomUUID());
        marketingUser.setName("Marketing Approver");

        // This is the user attached to the customer (e.g. applicant)
        User customerUser = new User();
        customerUser.setIdUser(UUID.randomUUID());
        customerUser.setName("Asep");

        Customer customer = new Customer();
        customer.setIdCustomer(UUID.randomUUID());
        customer.setUser(customerUser);

        Application app = new Application();
        app.setId(appId);
        app.setCustomer(customer);
        app.setAmount(BigDecimal.valueOf(10000));
        app.setPurpose("Test Loan Approve");
        app.setStatus(ApplicationStatus.PENDING_MARKETING);

        when(applicationRepo.findById(appId)).thenReturn(Optional.of(app));

        // Act
        applicationService.progressStatus(appId, true, marketingUser);

        // Assert
        assertEquals(ApplicationStatus.PENDING_BRANCH_MANAGER, app.getStatus());
        assertEquals(marketingUser, app.getMarketingApprover());
        assertNotNull(app.getMarketingApprovalTime());

        verify(applicationRepo).save(app);
    }


    @Test
    void testProgressStatus_MarketingApprovalRejected() {
        UUID appId = UUID.randomUUID();

        User marketingUser = new User();
        marketingUser.setIdUser(UUID.randomUUID());
        marketingUser.setName("Marketing Should reject");

        Customer customer = new Customer();
        customer.setIdCustomer(UUID.randomUUID());
        customer.setUser(marketingUser);

        Application app = new Application();
        app.setId(appId);
        app.setCustomer(customer);
        app.setAmount(BigDecimal.valueOf(10000));
        app.setPurpose("Test Loan Reject");
        app.setStatus(ApplicationStatus.PENDING_MARKETING);

        when(applicationRepo.findById(appId)).thenReturn(Optional.of(app));

        applicationService.progressStatus(appId, false, marketingUser);

        assertEquals(ApplicationStatus.REJECTED, app.getStatus());
        assertEquals(marketingUser, app.getMarketingApprover());
        assertNotNull(app.getMarketingApprovalTime());

        verify(applicationRepo).save(app);
    }

    @Test
    void testProgressStatus_InvalidStatus_ThrowsException() {
        UUID appId = UUID.randomUUID();

        User user = new User();
        user.setIdUser(UUID.randomUUID());
        user.setName("throw exception Approver Test");

        Application app = new Application();
        app.setId(appId);
        app.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepo.findById(appId)).thenReturn(Optional.of(app));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            applicationService.progressStatus(appId, true, user);
        });

        System.out.println("Exception message: " + exception.getMessage()); // 🔍 Debug output

        assertTrue(exception.getMessage().contains("Cannot progress application with status"));

        verify(applicationRepo, never()).save(any());
    }

}
