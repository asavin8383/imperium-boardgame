package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.AccessToolParameter;
import lombok.Data;
import model.converters.AccessToolParameterConverter;

import javax.persistence.*;

@Entity
@Data
@Table(schema = "config", name = "robots_properties")
public class RobotProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = AccessToolParameterConverter.class)
    private AccessToolParameter key;

    @Column(nullable = false)
    private String value;

    @ManyToOne(optional=false)
    @JoinColumn(foreignKey = @ForeignKey(name = "robots_properties_robots_id_fk"))
    private Robot robot;
}
