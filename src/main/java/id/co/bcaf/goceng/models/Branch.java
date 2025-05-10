package id.co.bcaf.goceng.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "branch")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_branch")
    private UUID idBranch;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String province;

    // Latitude and longitude for the branch
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Assuming you might want to add relationships in the future:

    // Example of one-to-many relationship to another branch (e.g., parent-child branches)
    @OneToMany(mappedBy = "parentBranch")
    @JsonManagedReference  // Prevent recursive serialization by marking this side as managed
    private List<Branch> childBranches;

    // Example of many-to-one relationship (e.g., branch manager or employee belongs to a branch)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference  // Prevent recursive serialization by marking this side as back
    private Branch parentBranch;

    public Branch(UUID uuid, String jakartaPusat, String s, String jakarta, String dkiJakarta, double v, double v1) {
    }
}
