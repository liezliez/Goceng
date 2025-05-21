package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_customer", nullable = false) // changed from customer_id
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "id_application", nullable = false) // changed from application_id
    private Application application;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer tenor;

    @Column(nullable = false)
    private BigDecimal installment;

    @Setter
    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(name = "remaining_tenor", nullable = false)
    private Integer remainingTenor;

    @Column(name = "remaining_principal", nullable = false)
    private BigDecimal remainingPrincipal;

    @Column(name = "total_paid", nullable = false)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    public enum LoanStatus {
        ACTIVE, PAID_OFF, DEFAULTED
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
