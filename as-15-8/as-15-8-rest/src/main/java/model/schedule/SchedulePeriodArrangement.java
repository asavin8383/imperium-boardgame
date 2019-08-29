package model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import model.task.Arrangement;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "schedule_period_arrangements", schema = "schedule")
@EqualsAndHashCode(exclude = "schedulePeriodCheckUnits")
@ToString(exclude = "schedulePeriodCheckUnits")
public class SchedulePeriodArrangement {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedule_period_arrangements_generator")
    @SequenceGenerator(name="schedule_period_arrangements_generator", schema= "schedule", sequenceName="schedule_period_arrangements_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_period_id")
    @NonNull
    private SchedulePeriod schedulePeriod;

    @ManyToOne(optional=false)
    @JoinColumn(name="arrangement_id")
    @NonNull
    private Arrangement arrangement;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedulePeriodArrangement", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<SchedulePeriodCheckUnit> schedulePeriodCheckUnits;

    @JsonView(Views.Brief.class)
    private Integer workersCount;

    @JsonView(Views.Brief.class)
    public Long getArrangementId(){
        return arrangement.getId();
    }
}
