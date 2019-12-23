package model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "schedule_period_check_units", schema = "schedule")
public class SchedulePeriodCheckUnit {

    @Id
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_period_arrangement_id")
    private SchedulePeriodArrangement schedulePeriodArrangement;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private ScheduleCheckUnit checkUnit;

    @Column(nullable = false)
    private Long executionNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SchedulePeriodCheckUnitStatus status;

    public SchedulePeriodCheckUnit(){
        this.status = SchedulePeriodCheckUnitStatus.READY;
    }
}
