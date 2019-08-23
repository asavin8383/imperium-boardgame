package model.schedule;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import model.Views;
import model.enums.SchedulePeriodCheckUnitStatus;
import model.result.ArrangementResult;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "schedule_period_check_units", schema = "schedule", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_period_arrangement_id", "executionNumber"}))
public class SchedulePeriodCheckUnit {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedule_period_check_units_generator")
    @SequenceGenerator(name="schedule_period_check_units_generator", schema= "schedule", sequenceName="schedule_period_check_units_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_period_arrangement_id")
    private SchedulePeriodArrangement schedulePeriodArrangement;

    @OneToOne(optional = false)
    private ArrangementResult checkUnit;

    @Column(nullable = false)
    private Long executionNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SchedulePeriodCheckUnitStatus status;

    public SchedulePeriodCheckUnit(){
        this.status = SchedulePeriodCheckUnitStatus.READY;
    }
}
