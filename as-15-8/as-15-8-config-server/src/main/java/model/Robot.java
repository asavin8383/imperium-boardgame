package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.AccessToolUnit;
import lombok.Data;
import model.converters.AccessToolUnitConverter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(schema = "config", name = "robots")
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = AccessToolUnitConverter.class)
    private AccessToolUnit accessTool;

    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(schema = "config", name = "robots_configurations",
            joinColumns = @JoinColumn(name = "robot_id", foreignKey = @ForeignKey(name = "robots_robots_configurations_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "configuration_id", foreignKey = @ForeignKey(name = "configurations_robots_configurations_id_fk")))
    private final Set<Configuration> configurations;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "robot")
    private final Set<RobotProperty> robotProperties = new HashSet<>();
}
