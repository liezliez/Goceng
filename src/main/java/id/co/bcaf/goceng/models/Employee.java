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
    @JsonBackReference("branch-employee")  // Explicit name to match Branch side
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonBackReference("user-employee")    // Matches User side
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus workStatus;


    // Optimistic locking
    @Version
    private int version;

    // Preventing infinite recursion during serialization with toString
    @Override
    public String toString() {
        return "Employee{id_employee=" + id + ", NIP='" + NIP + "', name='" + name + "', branch=" + branch + ", workStatus=" + workStatus + '}';
    }
}
