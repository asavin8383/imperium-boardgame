package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import robots.SlaPeriod;
import robots.SlaType;

import javax.persistence.*;

@Entity
@Data
@Table(schema = "config", name = "robots_sla", uniqueConstraints = @UniqueConstraint(columnNames = {"robot_id", "slaType", "slaPeriod"}))
@NoArgsConstructor
@RequiredArgsConstructor
public class RobotSLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Brief.class)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlaType slaType;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlaPeriod slaPeriod;

    @NonNull
    @Column(nullable = false)
    private Long checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(foreignKey = @ForeignKey(name = "robots_sla_robots_id_fk"))
    private Robot robot;
}