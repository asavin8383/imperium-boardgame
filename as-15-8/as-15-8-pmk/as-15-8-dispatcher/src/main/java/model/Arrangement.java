package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.ArrangementStatus;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "results", name = "arrangements")
public class Arrangement {

    private Long id;

    private ArrangementStatus status;

    private Long checkUnitsCount;

}
