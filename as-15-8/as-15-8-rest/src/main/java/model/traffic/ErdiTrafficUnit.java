package model.traffic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.catalog.AccessToolsCategory;
import model.enums.TrafficUnitType;
import model.sor.FormalErdi;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import utils.TrafficUnitUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "erdi_traffic_units")
@PrimaryKeyJoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
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

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(schema = "portal", name = "erdi_traffic_units_custom_erdi",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_erdi_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<CustomErdi> customErdiList;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(schema = "portal", name = "erdi_traffic_units_content",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<FormalErdi> formalErdiList;

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(getName()) && category == null &&
                CollectionUtils.isEmpty(customErdiList) &&
                CollectionUtils.isEmpty(formalErdiList);
    }

    @Override
    public TrafficUnitType getType() {
        return isEmpty() ? null : StringUtils.isEmpty(getName()) ?
                (CollectionUtils.isEmpty(formalErdiList) ?
                        TrafficUnitType.CUSTOM : TrafficUnitType.FORMAL) :
                TrafficUnitUtils.getType(this);
    }
}
