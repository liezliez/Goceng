package id.co.bcaf.goceng.dto;

import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.models.Customer;
import id.co.bcaf.goceng.models.Plafon;
import id.co.bcaf.goceng.repositories.BranchRepository;
import id.co.bcaf.goceng.repositories.CustomerRepository;
import id.co.bcaf.goceng.repositories.PlafonRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private BigDecimal amount;
    private String purpose;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Updated field names to reflect new naming conventions
    private String marketingAssignedName;
    private String branchManagerAssignedName;
    private String backOfficeAssignedName;

    private LocalDateTime marketingAssignedTime;
    private LocalDateTime branchManagerAssignedTime;
    private LocalDateTime backOfficeAssignedTime;

    private Integer tenor;
    private String limitType;
    private BigDecimal limitAmount;
    private BigDecimal interestRate;
    private String noteMarketing;
    private String noteBranchManager;
    private String noteBackOffice;
    private String nipMarketing;
    private String nipBranchManager;
    private String nipBackOffice;

    // Adding the toApplication method
    @Autowired
    private static BranchRepository branchRepo;

    @Autowired
    private static CustomerRepository customerRepo;

    @Autowired
    private static PlafonRepository plafonRepo;

    public Application toApplication() {
        Application app = new Application();
        app.setId(this.id);
        app.setCustomer(customerRepo.findById(this.customerId).orElseThrow(() -> new RuntimeException("Customer not found")));
        app.setAmount(this.amount);
        app.setPurpose(this.purpose);
        app.setTenor(this.tenor);
        app.setBranch(branchRepo.findById(this.customerId) // You may need to adjust the branch setting logic
                .orElseThrow(() -> new RuntimeException("Branch not found")));
        app.setStatus(ApplicationStatus.valueOf(this.status)); // Assuming ApplicationStatus is an enum
        app.setCreatedAt(this.createdAt);
        app.setUpdatedAt(this.updatedAt);
        app.setPlafonType(this.limitType);
        app.setPlafonLimit(this.limitAmount);
        app.setInterestRate(this.interestRate);

        // Set other fields if necessary
        // Assuming you've added proper methods to set these fields
        app.setMarketingNote(this.noteMarketing);
        app.setBranchManagerNote(this.noteBranchManager);
        app.setBackOfficeNote(this.noteBackOffice);

        return app;
    }
}
