package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.enums.ArrangementStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**Мероприятие в рамках формализованного задания*/

@Entity
@Table(schema="schedule", name="arrangements")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Arrangement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    /**Название мероприятия*/
    @NotNull
    @ToString.Include
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private String title;

    /**Дата создания*/
    @ToString.Include
    @JsonView(Views.Brief.class)
    private LocalDateTime creationDate;

    /**Плановая дата начала*/
    @JsonView(Views.Brief.class)
    private LocalTime plannedStartTime;
    /**Плановая дата окончания*/
    @JsonView(Views.Brief.class)
    private LocalTime plannedEndTime;

    /** Максимальное количество обработчиков мероприятия */
    @JsonView(Views.Brief.class)
    private Integer maxWorkersCount;

    /** ПС/ПАСД мероприятия*/
    @JsonView(Views.Brief.class)
    @ToString.Include
    @NotNull
    private String accessTool;

    @Column(nullable = false, columnDefinition = "text default 'NEW'")
    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ArrangementStatus status;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement")
    @JsonIgnore
    private final List<ScheduleCheckUnit> scheduleCheckUnits = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement")
    @JsonIgnore
    private final List<SchedulePeriodArrangement> schedulePeriodArrangements = new ArrayList<>();
}
