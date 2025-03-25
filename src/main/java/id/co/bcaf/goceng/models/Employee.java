package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id_employee;

    @Column(nullable = false, unique = true)
    private String NIP;

    private String name;
    private String branch;
    private String workStatus;

    @Version  // Optimistic locking to prevent concurrent update issues
    private Long version;
}
