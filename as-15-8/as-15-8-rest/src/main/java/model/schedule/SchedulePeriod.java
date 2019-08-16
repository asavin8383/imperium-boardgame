package model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import model.Views;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "schedule_periods", schema = "schedule")
public class SchedulePeriod {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="schedule_periods_generator")
    @SequenceGenerator(name="schedule_periods_generator", schema= "schedule", sequenceName="schedule_periods_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_id")
    private Schedule schedule;

    @NonNull
    private LocalTime startTime;

    @NonNull
    private LocalTime endTime;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "schedulePeriod", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<SchedulePeriodArrangement> schedulePeriodArrangements = new ArrayList<>();
}
