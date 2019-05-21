package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import enums.ArrangementUnitCheckResult;
import jobs.CheckUnitType;
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
    @GeneratedValue(strategy=GenerationType.TABLE)
    @Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
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