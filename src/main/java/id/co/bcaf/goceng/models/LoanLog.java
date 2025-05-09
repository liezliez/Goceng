package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_loan_log")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "action", nullable = false)
    private String action; // e.g., UPDATE, CREATE, DELETE

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "performed_by")
    private String performedBy;
}
