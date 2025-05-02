package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_features")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_role_feature;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_feature", nullable = false)
    private Feature feature;

    /**
     * Checks if both role and feature are present in this association.
     *
     * @return true if both role and feature are non-null, false otherwise.
     */
    public boolean isPresent() {
        return role != null && feature != null;
    }
}
