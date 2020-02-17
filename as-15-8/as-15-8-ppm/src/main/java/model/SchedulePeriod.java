package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.enums.SchedulePeriodState;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "schedule_periods", schema = "schedule")
@ToString(exclude = "schedulePeriodArrangements")
@EqualsAndHashCode(exclude = "schedulePeriodArrangements")
public class SchedulePeriod implements Comparable<SchedulePeriod>{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_id")
    @NonNull
    private Schedule schedule;

    @NonNull
    @JsonView(Views.Brief.class)
    private LocalTime startTime;

    @NonNull
    @JsonView(Views.Brief.class)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    private SchedulePeriodState schedulePeriodState = SchedulePeriodState.CREATED;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedulePeriod")
    @JsonView(Views.Full.class)
    private Set<SchedulePeriodArrangement> schedulePeriodArrangements = new HashSet<>();

    @Override
    public int compareTo(SchedulePeriod other) {
        return Objects.compare(this, other, Comparator.comparing(SchedulePeriod::getStartTime));
    }
}
