package model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.ArrangementUnitCheckResult;
import jobs.CheckUnitType;
import lombok.Data;
import model.erdi.ERDI;
import model.task.Arrangement;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_results")
@Data
public class ArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE)
    @Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "FK_arrangement_results_job_id"))
    @JsonIgnore
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id", foreignKey = @ForeignKey(name = "arrangement_results_content_id_fk"))
    @JsonIgnore
    private ERDI ERDI;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    private ArrangementUnitCheckResult result;

    @Lob
    @Type(type="org.hibernate.type.BinaryType")
    @Column(name="screenshot", columnDefinition="bytea")
    private byte[] screenshot;


}
