package model;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import model.enums.ScheduleCheckUnitStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(schema = "schedule", name = "schedule_check_units")
@Data
public class ScheduleCheckUnit implements Serializable, Comparable<ScheduleCheckUnit> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Arrangement arrangement;

    private Long erdiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CheckUnitType checkUnitType;

    @Column(nullable=false)
    private String checkUnitValue;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean finished = false;

    @Override
    public int compareTo(ScheduleCheckUnit o) {
        return Objects.compare(this, o, Comparator.comparing(ScheduleCheckUnit::getId));
    }
}
