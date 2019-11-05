package model;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "schedule", name = "schedule_check_units")
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

    @Column(nullable=false)
    private Long erdiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CheckUnitType checkUnitType;

    @Column(nullable=false)
    private String checkUnitValue;
}
