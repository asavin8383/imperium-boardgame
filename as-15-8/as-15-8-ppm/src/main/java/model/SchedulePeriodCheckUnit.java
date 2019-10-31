package model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "schedule_period_check_units", schema = "schedule", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_period_arrangement_id", "executionNumber"}))
public class SchedulePeriodCheckUnit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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
