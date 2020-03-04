package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.ArrangementStatus;
import model.enums.Reason;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "results", name = "arrangements")
public class Arrangement {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArrangementStatus status;

    @Column(nullable = false)
    private Long checkUnitsCount;

    @Column(nullable = false)
    private Long maxCheckUnitsCount;

    @Column (nullable = false)
    private Boolean isManual = false;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Reason reason = Reason.MANUAL;
}
