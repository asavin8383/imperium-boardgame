package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.enums.ArrangementExecutionStatus;
import model.erdi.ERDI;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 * Информация о выполнении мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_execution",
        uniqueConstraints = @UniqueConstraint(name = "uq_arrangement_execution_arrangement_id_content_id", columnNames={"arrangement_id", "content_id"}))
@Data
public class ArrangementExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="arrangement_execution_generator")
    @SequenceGenerator(name="arrangement_execution_generator", schema="portal", sequenceName="arrangement_execution_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "arrangement_execution_arrangements_id_fk"))
    @JsonIgnore
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(name = "arrangement_execution_content_id_fk"))
    private ERDI erdi;

    /**Количество проверяемых единиц*/
    private int checkUnitCount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ArrangementExecutionStatus status;
}
