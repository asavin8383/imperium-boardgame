package model.schedule;

import checkUnits.CheckMethod;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Моедль планирования времени обработки запрещенных ресурсов
 * Creation date: 07.08.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "planned_processing_times")
@Data
public class PlannedProcessingTime implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="planned_processing_times_generator")
    @SequenceGenerator(name="planned_processing_times_generator", schema="portal", sequenceName="planned_processing_times_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    @JsonView(Views.Id.class)
    private Long id;

    @JsonView(Views.Brief.class)
    @NotNull
    private String accessTool;

    @Enumerated(EnumType.STRING)
    @NotNull
    @JsonView(Views.Full.class)
    @Column(name = "check_method")
    private CheckMethod checkMethod;

    @JsonView(Views.Full.class)
    private long plannedProcessingTimeMs;

}
