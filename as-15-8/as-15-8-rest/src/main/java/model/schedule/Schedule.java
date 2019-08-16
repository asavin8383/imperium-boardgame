package model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import model.Views;
import model.enums.ScheduleStatus;
import model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "schedules", schema = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedules_generator")
    @SequenceGenerator(name="schedules_generator", schema= "schedule", sequenceName="schedules_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @NotNull
    @JsonView(Views.Full.class)
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
    private ScheduleStatus status;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedule", fetch = FetchType.EAGER)
    @JsonView(Views.Full.class)
    private List<SchedulePeriod> schedulePeriods = new ArrayList<>();

    public Schedule(){
        this.creationDate = LocalDateTime.now();
        this.status = ScheduleStatus.NEW;
    }
}
