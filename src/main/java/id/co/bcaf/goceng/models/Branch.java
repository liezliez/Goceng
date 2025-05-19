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
    private UUID id;

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

    // Self-referencing OneToMany relationship for child branches
    @OneToMany(mappedBy = "parentBranch")
    @JsonManagedReference("branch-childBranches")  // Named reference to avoid conflicts
    private List<Branch> childBranches;

    // Self-referencing ManyToOne relationship for parent branch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference("branch-childBranches")    // Must match the managed reference name
    private Branch parentBranch;

    public Branch(UUID id, String name, String address, String city, String province, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.province = province;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
