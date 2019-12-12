package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolParameter;
import lombok.Data;
import model.converters.AccessToolParameterConverter;

import javax.persistence.*;

@Entity
@Data
@Table(schema = "config", name = "global_properties")
public class GlobalProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @JsonView(Views.Brief.class)
    @Column(nullable = false)
    private String key;

    @JsonView(Views.Brief.class)
    @Column(nullable = false)
    private String value;

    @ManyToOne(optional=false)
    @JoinColumn(foreignKey = @ForeignKey(name = "global_properties_configurations_id_fk"))
    private Configuration configuration;
}
