package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "traffic")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @OneToMany(mappedBy = "traffic", orphanRemoval = true,
            cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TrafficUnit> trafficUnits;



    @Transient
    @JsonView(Views.Full.class)
    private ErdiTrafficUnit formalErdiUnit;

    @Transient
    @JsonView(Views.Full.class)
    private ErdiTrafficUnit customErdiUnit;

    @Transient
    @JsonView(Views.Full.class)
    private SearchQueryTrafficUnit searchPhraseUnit;

    @Transient
    @JsonView(Views.Full.class)
    private List<SearchQueryTrafficUnit> searchTemplates;

}
