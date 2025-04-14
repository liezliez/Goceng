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

    @ManyToOne
    @JoinColumn(name = "id_marketing_approver")
    private User marketingApprover;

    @ManyToOne
    @JoinColumn(name = "id_branch_manager_approver")
    private User branchManagerApprover;

    @ManyToOne
    @JoinColumn(name = "id_back_office_approver")
    private User backOfficeApprover;

    @Column(name = "marketing_approval_time")
    private LocalDateTime marketingApprovalTime;

    @Column(name = "branch_manager_approval_time")
    private LocalDateTime branchManagerApprovalTime;

    @Column(name = "back_office_approval_time")
    private LocalDateTime backOfficeApprovalTime;

    // New field to store the proposed branch
    private String branch;


    // In Application.java
    public UUID getUserId() {
        return customer.getUser().getIdUser();
    }

    public String getCustomerName() {
        return customer.getUser().getName();
    }
}

