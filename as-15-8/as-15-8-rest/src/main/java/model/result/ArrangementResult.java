package model.result;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import lombok.Data;
import model.erdi.ERDI;
import model.task.Arrangement;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_results")
@Data
public class ArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="arrangement_results_generator")
    @SequenceGenerator(name="arrangement_results_generator", schema="portal", sequenceName="arrangement_results_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "FK_arrangement_results_job_id"))
    @JsonIgnore
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id", foreignKey = @ForeignKey(name = "FK_arrangement_results_content_id"))
    @JsonIgnore
    private ERDI erdi;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    private CheckUnitJobResult result;

    /*Lob
    @Column(name="screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    @JsonIgnore
    private byte[] screenshot;

    @Lob
    @Column(name="etalon_screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    @JsonIgnore
    private byte[] etalonScreenshot;*/

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    @JsonIgnore
    DetailedArrangementResult detailedArrangementResult;

}
