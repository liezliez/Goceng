package id.co.bcaf.goceng.models;

import id.co.bcaf.goceng.enums.WorkStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id_employee;

    @Column(nullable = false, unique = true)
    private String NIP;

    private String name;
    private String branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus workStatus;

    @OneToOne
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;
}
