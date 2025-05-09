package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "loan_limit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_loan_limit")
    private UUID id;

    @Column(name = "limit_type", nullable = false)
    private String limitType; // e.g. "Standard", "Premium", etc.

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
}
