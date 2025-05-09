package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, APPROVE, etc.

    @Column(nullable = false)
    private String changedBy;

    private String beforeStatus;

    private String afterStatus;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String details;
}
