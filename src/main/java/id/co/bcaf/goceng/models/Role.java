package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID as primary key
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
