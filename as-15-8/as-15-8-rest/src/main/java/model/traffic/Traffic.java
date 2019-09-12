package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.task.Arrangement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "traffic")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Traffic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "traffic_generator")
    @SequenceGenerator(name = "traffic_generator",
            schema = "portal", sequenceName = "traffic_id_seq", allocationSize = 1)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Arrangement arrangement;

    @OneToMany(mappedBy = "traffic")
    // cascade nothing, fetch lazy, orphanRemoval = false
    // @JoinColumn(name = "traffic_id")
    @JsonIgnore
    private List<TrafficUnit> trafficUnits;

}
