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
import java.util.ArrayList;
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

    @NotNull
    @Column (nullable = false)
    @ToString.Include
    private Long actualCheckUnitsCount = 0L;

    @NotNull
    @Column (nullable = false)
    @ToString.Include
    private Long erdiCount = 0L;

    @OneToMany(mappedBy = "traffic", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<ErdiTrafficUnit> erdiTrafficUnits = new ArrayList<>();

    @OneToMany(mappedBy = "traffic", orphanRemoval = true,
            cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SearchQueryTrafficUnit> searchQueryTrafficUnits = new ArrayList<>();

    @OneToMany(mappedBy = "traffic", orphanRemoval = true,
            cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DynamicTrafficUnit> dynamicTrafficUnits = new ArrayList<>();

    public void addErdiTrafficUnit(ErdiTrafficUnit erdiTrafficUnit) {
        this.erdiTrafficUnits.add(erdiTrafficUnit);
    }

}
