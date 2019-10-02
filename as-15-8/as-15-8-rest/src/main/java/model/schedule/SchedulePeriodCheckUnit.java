package model.schedule;

import lombok.Data;
import model.enums.SchedulePeriodCheckUnitStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "schedule_period_check_units", schema = "schedule", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_period_arrangement_id", "executionNumber"}))
public class SchedulePeriodCheckUnit {

    @Id
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_period_arrangement_id")
    private SchedulePeriodArrangement schedulePeriodArrangement;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @Column(name = "check_unit_id")
    @MapsId
    private ScheduleCheckUnit scheduleCheckUnit;

    @Column(nullable = false)
    private Long executionNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SchedulePeriodCheckUnitStatus status;

    public SchedulePeriodCheckUnit(){
        this.status = SchedulePeriodCheckUnitStatus.READY;
    }
}
