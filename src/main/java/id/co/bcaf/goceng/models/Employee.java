package id.co.bcaf.goceng.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private UUID id;

    @Column(nullable = false, unique = true)
    private String NIP;

    private String name;

    @ManyToOne
    @JoinColumn(name = "id_branch")
    @JsonBackReference  // Prevent recursion with Branch (parent side)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus workStatus;

    @OneToOne
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    @JsonBackReference  // Prevent recursion with User (back reference)
    private User user;

    // Optimistic locking
    @Version
    private int version;

    // Preventing infinite recursion during serialization with toString
    @Override
    public String toString() {
        return "Employee{id_employee=" + id + ", NIP='" + NIP + "', name='" + name + "', branch=" + branch + ", workStatus=" + workStatus + '}';
    }
}
