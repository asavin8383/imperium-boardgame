package model.schedule;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.enums.ScheduleStatus;
import model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "schedules", schema = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedules_generator")
    @SequenceGenerator(name="schedules_generator", schema= "schedule", sequenceName="schedules_id_seq", allocationSize=1)
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

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    @JsonView(Views.Full.class)
    private User user;

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
}
