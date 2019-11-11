package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "portal", name = "erdi_traffic_units_content")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ErdiTrafficUnitContent implements Serializable {

    private static final long serialVersionUID = 1490702373249772220L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private ErdiTrafficUnit trafficUnit;

    // для единообразия json при сохранении трафика
    @JsonProperty("id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long contentId;

    public ErdiTrafficUnitContent() { }

    public ErdiTrafficUnitContent(ErdiTrafficUnit trafficUnit, Long contentId) {
        this.trafficUnit = trafficUnit;
        this.contentId = contentId;
    }
}
