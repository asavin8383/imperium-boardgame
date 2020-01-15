package model;

import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import lombok.Data;
import model.enums.CheckType;
import model.enums.UserResult;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 * Результат проведения мероприятия по одноу из URL, входящих в состав ЕРДИ
 */
@Entity
@Table(schema = "results", name = "results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"arrangement_id", "content_id", "check_unit_type", "check_unit_value"}))
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable=false, updatable=false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "FK_arrangements"))
    private Arrangement arrangement;

    @Column(name = "content_id")
    private Long erdiId;

    @Enumerated(EnumType.STRING)
    private CheckType checkType;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    private CheckUnitJobResult result;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean checkForAct;

    @Enumerated(EnumType.STRING)
    private UserResult userResult;

    private String userDescription;

    public Result() {
        this.startDate = LocalDateTime.now();
    }
}
