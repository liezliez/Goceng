package id.co.bcaf.goceng.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class ApplicationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String changedBy;

    private String beforeStatus;

    private String afterStatus;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String details;
}
