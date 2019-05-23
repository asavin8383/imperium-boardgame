package model;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import checkUnits.CheckUnitType;
import enums.ArrangementUnitCheckResult;
import lombok.Data;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_results")
@Data
public class ArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="arrangement__results_generator")
    @SequenceGenerator(name="arrangement__results_generator", schema="portal", sequenceName="arrangement__results_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @Column(name="arrangement_id", nullable=false)
    private Long arrangementId;

    @Column(name="content_id", nullable=false)
    private Long erdiId;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;
    
    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    private ArrangementUnitCheckResult result;

    @Lob
    @Column(name="screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] screenshot = new byte[0];
}