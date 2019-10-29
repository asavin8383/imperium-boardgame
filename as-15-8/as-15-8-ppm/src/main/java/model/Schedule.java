package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "schedules", schema = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @JsonView(Views.Full.class)
    @ToString.Include
    private LocalDate plannedDate;

    @NotNull
    @JsonView(Views.Full.class)
    private LocalDateTime creationDate;

    @NotNull
    @JsonView(Views.Full.class)
    private String user;

    @Enumerated(EnumType.STRING)
    @NotNull
    @JsonView(Views.Full.class)
    @ToString.Include
    private ScheduleStatus status;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedule", fetch = FetchType.EAGER)
    @JsonView(Views.Full.class)
    @OrderBy("startTime")
    private SortedSet<SchedulePeriod> schedulePeriods = new TreeSet<>(Comparator.comparing(SchedulePeriod::getStartTime));

    public Schedule(){
        this.creationDate = LocalDateTime.now();
        this.status = ScheduleStatus.NEW;
    }

    public TreeSet<SchedulePeriod> getSchedulePeriodsAsTreeSet(){
        return (TreeSet)this.getSchedulePeriods();
    }
}
