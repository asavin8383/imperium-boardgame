package model.traffic.jointable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(schema = "portal", name = "erdi_traffic_units_content")
@Data
@Setter(AccessLevel.PRIVATE)
public class ErdiTrafficUnitContent {

    @Id
    private Long id;

    @Column(name = "traffic_unit_id")
    private Long trafficUnitId;

    @Column(name = "content_id")
    private Long contentId;

}
