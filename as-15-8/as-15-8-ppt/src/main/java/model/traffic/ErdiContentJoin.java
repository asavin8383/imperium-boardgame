package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(schema = "portal", name = "erdi_traffic_units_content")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ErdiContentJoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    @ToString.Include
    private ErdiTrafficUnit trafficUnit;

    // для единообразия json при сохранении трафика
    @JsonProperty("id")
    @ToString.Include
    private Long contentId;

}
