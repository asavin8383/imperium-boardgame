package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.converters.AccessToolParameterConverter;

import javax.persistence.*;

@Entity
@Data
@Table(schema = "config", name = "robots_properties")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RobotProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AccessToolParameterConverter.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private AccessToolParameter key;

    @Column(nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private String value;

    @ManyToOne(optional=false)
    @JoinColumn(foreignKey = @ForeignKey(name = "robots_properties_robots_id_fk"))
    private Robot robot;
}
