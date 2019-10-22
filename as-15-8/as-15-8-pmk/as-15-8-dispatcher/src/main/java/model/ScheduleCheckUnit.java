package model;

import checkUnits.CheckUnitType;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "schedule", name = "schedule_check_units")
@Data
public class ScheduleCheckUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arrangement_id", nullable = false)
    private Long arrangementId;

    @Column(name = "content_id", nullable = false)
    private Long erdiId;

    @Enumerated(EnumType.STRING)
    @Column(name="check_unit_type", nullable=false)
    private CheckUnitType checkUnitType;

    @Column(name="check_unit_value", nullable=false)
    private String checkUnitValue;

}
