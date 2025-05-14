package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "plafon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plafon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "plafon_type", nullable = false)
    private String plafonType;

    @Column(name = "plafon_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal plafonAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
}
