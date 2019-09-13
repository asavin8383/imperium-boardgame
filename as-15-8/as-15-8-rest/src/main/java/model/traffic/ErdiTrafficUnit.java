package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.catalog.AccessToolsCategory;
import model.erdi.FormalErdi;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "erdi_traffic_units")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ErdiTrafficUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long trafficUnitId;

    @MapsId
    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    // fetch eager, mappedBy = ?
    // @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private TrafficUnit trafficUnit;

    @ManyToOne(optional = false)
    // cascade nothing, fetch eager
    @JoinColumn(name = "category_id", nullable = false)
    @JsonView(Views.Brief.class)
    // @ToString.Include
    private AccessToolsCategory category;

    @ManyToMany // cascade nothing, fetch lazy
    @JoinTable(schema = "portal", name = "erdi_traffic_units_custom_erdi",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_erdi_id"))
    @JsonIgnore
    private List<CustomErdi> customErdiList;

    @ManyToMany // cascade nothing, fetch lazy
    @JoinTable(schema = "portal", name = "erdi_traffic_units_content",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id"))
    @JsonIgnore
    private List<FormalErdi> formalErdiList;

}
