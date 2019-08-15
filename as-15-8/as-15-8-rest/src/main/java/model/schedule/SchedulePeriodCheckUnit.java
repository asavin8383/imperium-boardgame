package model.schedule;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import model.Views;
import model.task.Arrangement;

import javax.persistence.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "schedule_period_check_units", schema = "portal")
public class SchedulePeriodCheckUnit {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedule_period_check_units_generator")
    @SequenceGenerator(name="schedule_period_check_units_generator", schema="portal", sequenceName="schedule_period_check_units_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_period_id", foreignKey = @ForeignKey(name = "schedule_period_check_units_schedule_periods_id_fk"))
    private SchedulePeriod schedulePeriod;

    @ManyToOne(optional=false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "schedule_period_check_units_arrangements_id_fk"))
    @NonNull
    private Arrangement arrangement;

    private Integer workersCount;
}
