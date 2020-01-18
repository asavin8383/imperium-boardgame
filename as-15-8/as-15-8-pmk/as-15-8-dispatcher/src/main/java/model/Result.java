package model;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "FK_arrangements"))
    @JsonView(Views.Full.class)
    private Arrangement arrangement;

    @Column(name = "content_id")
    @JsonView(Views.Brief.class)
    private Long erdiId;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Brief.class)
    private CheckType checkType;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    @JsonView(Views.Brief.class)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    @JsonView(Views.Brief.class)
    private String checkUnitValue;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    @JsonView(Views.Brief.class)
    private CheckUnitJobResult result;

    @JsonView(Views.Brief.class)
    private LocalDateTime startDate;

    @JsonView(Views.Brief.class)
    private LocalDateTime endDate;

    @JsonView(Views.Brief.class)
    private boolean checkForAct;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Full.class)
    private UserResult userResult;

    @JsonView(Views.Full.class)
    private String userDescription;

    public Result() {
        this.startDate = LocalDateTime.now();
    }

    @OneToOne(
            mappedBy = "result",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private ResultScreenShot resultScreenShot;

    @OneToOne(
            mappedBy = "result",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private PsDetailResult psDetailResult;

    @OneToOne(
            mappedBy = "result",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private PasdDetailResult pasdDetailResult;

    @OneToOne(
            mappedBy = "result",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private NmapDetailResult nmapDetailResult;

    @OneToOne(
            mappedBy = "result",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private ErrorDetailResult errorDetailResult;

    public void setDetailResult(DetailResult detailResult){
        if(detailResult instanceof PsDetailResult)
            this.psDetailResult = (PsDetailResult) detailResult;
        else if(detailResult instanceof PasdDetailResult)
            this.pasdDetailResult = (PasdDetailResult) detailResult;
        else if(detailResult instanceof NmapDetailResult)
            this.nmapDetailResult = (NmapDetailResult) detailResult;
        else if(detailResult instanceof ErrorDetailResult)
            this.errorDetailResult = (ErrorDetailResult) detailResult;
    }
}
