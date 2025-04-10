package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "features")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_feature;

    @Column(nullable = false, unique = true, length = 100)
    private String feature_name; // e.g., "VIEW_USERS", "EDIT_USERS"
}
