package model;

import checkUnits.CheckUnitType;
import enums.ArrangementUnitCheckResult;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 * Результат проведения мероприятия по одноу из URL, входящих в состав ЕРДИ
 */
@Entity
@Table(schema = "portal", name = "arrangement_results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"arrangament_id", "erdi_id", "check_unit_type", "check_unit_value"}))
@Data
public class ArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="arrangement__results_generator")
    @SequenceGenerator(name="arrangement__results_generator", schema="portal", sequenceName="arrangement_results_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    private Long arrangement_id;

    private Long erdi_id;

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
    private byte[] screenshot;
}
