package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Customer {

    @Id
    @GeneratedValue
    @Column(name = "id_customer", updatable = false, nullable = false)
    private UUID idCustomer;

    @OneToOne
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "account_no", unique = true)
    private String accountNo;

    @Column(name = "nik", unique = true, nullable = false)
    private String nik;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "telp_no")
    private String telpNo;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "mother_maiden_name")
    private String motherMaidenName;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "home_ownership_status")
    private String homeOwnershipStatus;

    @Column(name = "emergency_call")
    private String emergencyCall;
}
