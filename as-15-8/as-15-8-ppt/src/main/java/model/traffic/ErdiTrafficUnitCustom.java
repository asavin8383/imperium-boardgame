package model.traffic;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Table(schema = "portal", name = "erdi_traffic_units_custom_erdi")
@Data
@Setter(AccessLevel.PRIVATE)
public class ErdiTrafficUnitCustom implements Serializable {

    private static final long serialVersionUID = -7692317476147938998L;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    private ErdiTrafficUnit trafficUnit;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "custom_erdi_id", referencedColumnName = "id")
    private CustomErdi customErdi;
}
