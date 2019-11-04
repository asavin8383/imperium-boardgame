package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.sor.Violation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "custom_erdi")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomErdi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String name;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "violation_id")
    @JsonView(Views.Brief.class)
    private Violation violation;

    @OneToMany(mappedBy = "customErdi", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonView(Views.Full.class)
    private List<CustomErdiUnit> customErdiUnits;

    @ManyToMany(mappedBy = "customErdiList")
    @JsonIgnore
    private List<SearchQueryTrafficUnit> searchQueryTrafficUnits;

    @ManyToMany(mappedBy = "customErdiList")
    @JsonIgnore
    private List<ErdiTrafficUnit> erdiTrafficUnits;

}
