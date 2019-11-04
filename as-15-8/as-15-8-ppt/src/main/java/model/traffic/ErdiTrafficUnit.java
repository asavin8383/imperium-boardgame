package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.catalog.AccessToolsCategory;
import model.enums.TrafficUnitType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import utils.TrafficUnitUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "erdi_traffic_units")
@PrimaryKeyJoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
@OnDelete(action = OnDeleteAction.CASCADE)
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErdiTrafficUnit extends TrafficUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonView(Views.Brief.class)
    private AccessToolsCategory category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "traffic_id", nullable = false)
    @JsonIgnore
    @ToString.Include
    private Traffic traffic;

    @ManyToMany
    @JoinTable(schema = "portal", name = "erdi_traffic_units_custom_erdi",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_erdi_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<CustomErdi> customErdiList;

    @OneToMany(mappedBy = "trafficUnit",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<ErdiContentJoin> formalErdiList;

    @Override
    public boolean isEmpty() {
        return getId() == null && category == null &&
                StringUtils.isEmpty(getName()) &&
                CollectionUtils.isEmpty(customErdiList) &&
                CollectionUtils.isEmpty(formalErdiList);
    }

    @Override
    public TrafficUnitType getType() {
        if (isEmpty()) throw new IllegalStateException(
                "Cannot infer type for empty TrafficUnit");

        return StringUtils.isEmpty(getName()) ?
                (CollectionUtils.isEmpty(formalErdiList) ?
                        TrafficUnitType.CUSTOM : TrafficUnitType.FORMAL) :
                TrafficUnitUtils.getType(this);
    }

    @Override
    public void syncContentAssociation() {
        if (formalErdiList != null)
            formalErdiList.forEach(join -> join.setTrafficUnit(this));
    }
}
