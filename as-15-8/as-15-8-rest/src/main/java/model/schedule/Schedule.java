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
import java.util.List;

@Data
@Entity
@Table(name = "schedules", schema = "portal")
public class Schedule {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedules_generator")
    @SequenceGenerator(name="schedules_generator", schema="portal", sequenceName="schedules_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @NotNull
    private LocalDate plannedDate;

    @NotNull
    private LocalDateTime creationDate;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "schedules_users_id_fk"))
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ScheduleStatus status;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedule", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<SchedulePeriod> schedulePeriods;

    public Schedule(){
        this.creationDate = LocalDateTime.now();
    }
}
