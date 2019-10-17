package model.schedule;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import model.Views;
import model.erdi.ERDI;
import model.task.Arrangement;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "schedule", name = "schedule_check_units")
@Immutable
@Data
public class ScheduleCheckUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id")
    @JsonIgnore
    private ERDI erdi;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;
}
