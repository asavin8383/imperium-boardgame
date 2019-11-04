package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.AccessToolUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import model.converters.AccessToolUnitConverter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(schema = "config", name = "robots")
@NoArgsConstructor
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "robot_generator")
    @SequenceGenerator(name = "robot_generator", schema = "config", sequenceName = "robots_id_seq", allocationSize = 1)
    @JsonIgnore
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(nullable = true)
    @Convert(converter = AccessToolUnitConverter.class)
    private AccessToolUnit accessTool;

    @Column
    private Long orig_id;

    @Column
    private String orig_name;

    @Column
    @Enumerated(EnumType.STRING)
    private RobotType type;

    @Column
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(schema = "config", name = "robots_configurations",
            joinColumns = @JoinColumn(name = "robot_id", foreignKey = @ForeignKey(name = "robots_robots_configurations_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "configuration_id", foreignKey = @ForeignKey(name = "configurations_robots_configurations_id_fk")))
    private Set<Configuration> configurations;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(cascade=CascadeType.ALL, mappedBy = "robot")
    private final Set<RobotProperty> robotProperties = new HashSet<>();
}
