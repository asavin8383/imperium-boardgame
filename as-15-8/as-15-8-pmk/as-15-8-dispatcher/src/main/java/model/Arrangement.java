package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.ArrangementStatus;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "results", name = "arrangements")
public class Arrangement {

    @Id
    private Long id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArrangementStatus status;

    @Column(name = "checkUnitsCount", nullable = false)
    private Long checkUnitsCount;

}
