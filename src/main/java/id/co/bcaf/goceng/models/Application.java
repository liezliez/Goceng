package id.co.bcaf.goceng.models;

import id.co.bcaf.goceng.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "application")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_application", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_customer", nullable = false)
    private Customer customer;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(length = 255)
    private String purpose;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Updated field names for clarity
    @ManyToOne
    @JoinColumn(name = "id_marketing_assigned")
    private User marketingAssigned;

    @ManyToOne
    @JoinColumn(name = "id_branch_manager_assigned")
    private User branchManagerAssigned;

    @ManyToOne
    @JoinColumn(name = "id_back_office_assigned")
    private User backOfficeAssigned;

    @Column(name = "marketing_assigned_time")
    private LocalDateTime marketingAssignedTime;

    @Column(name = "branch_manager_assigned_time")
    private LocalDateTime branchManagerAssignedTime;

    @Column(name = "back_office_assigned_time")
    private LocalDateTime backOfficeAssignedTime;

    // Reference to Branch entity
    @ManyToOne
    @JoinColumn(name = "id_branch")
    private Branch branch;

    // In Application.java
    public UUID getUserId() {
        return customer.getUser().getIdUser();
    }

    public String getCustomerName() {
        return customer.getUser().getName();
    }
}


// Note dari Marketing, BM, BO
// Bikin DTO untuk semua detail pengajuan (application)