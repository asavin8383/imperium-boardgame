package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
class RobotProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AccessToolParameterConverter.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private AccessToolParameter key;

    @Column(nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String value;

    @ManyToOne(optional=false)
    @JoinColumn(foreignKey = @ForeignKey(name = "robots_properties_robots_id_fk"))
    private Robot robot;
}
