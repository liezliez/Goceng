package id.co.bcaf.goceng.models;

import id.co.bcaf.goceng.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)

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

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "plafon_type")
    private String plafonType;

    @Column(name = "plafon_limit")
    private BigDecimal plafonLimit;

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

    @Column(name = "marketing_note", columnDefinition = "TEXT")
    private String marketingNote;

    @Column(name = "branch_manager_note", columnDefinition = "TEXT")
    private String branchManagerNote;

    @Column(name = "back_office_note", columnDefinition = "TEXT")
    private String backOfficeNote;

    @Column(nullable = false)
    private Integer tenor;

    @ManyToOne
    @JoinColumn(name = "id_plafon", nullable = false)
    private Plafon plafon;

    @Column(name = "nip_marketing", length = 50)
    private String nipMarketing;

    @Column(name = "nip_branch_manager", length = 50)
    private String nipBranchManager;

    @Column(name = "nip_back_office", length = 50)
    private String nipBackOffice;

    @ManyToOne
    @JoinColumn(name = "id_branch")
    private Branch branch;

    public UUID getUserId() {
        return customer.getUser().getIdUser();
    }

    public String getCustomerName() {
        return customer.getUser().getName();
    }
}
