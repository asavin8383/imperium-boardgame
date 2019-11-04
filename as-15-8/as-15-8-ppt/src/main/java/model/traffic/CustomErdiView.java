package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import model.sor.Violation;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Setter(value = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Subselect("select erdi.id as id, \n" +
        "       erdi.name as name, \n" +
        "       erdi.violation_id as violation_id, \n" +
        "       unit.type as unit_type, \n" +
        "       unit.value as unit_value \n" +
        "from (select erdi.id as erdi_id, \n" +
        "             min(unit.id) as unit_id \n" +
        "      from portal.custom_erdi erdi \n" +
        "      left join portal.custom_erdi_units unit  \n" +
        "           on erdi.id = unit.custom_erdi_id \n" +
        "      group by erdi.id) as help \n" +
        "join portal.custom_erdi erdi \n" +
        "  on help.erdi_id = erdi.id \n" +
        "left join portal.custom_erdi_units unit \n" +
        "       on help.unit_id = unit.id")
public class CustomErdiView implements Serializable {

    public static final long serialVersionUID = 1;

    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    private String name;

    @ToString.Include
    @ManyToOne
    @JoinColumn(name = "violation_id",
            referencedColumnName = "id")
    private Violation violation;

    @ToString.Include
    private String unitType;

    @ToString.Include
    private String unitValue;

    @ManyToMany(mappedBy = "customErdiList")
    @JsonIgnore
    private List<SearchQueryTrafficUnit> searchQueryTrafficUnits;

    @ManyToMany(mappedBy = "customErdiList")
    @JsonIgnore
    private List<ErdiTrafficUnit> erdiTrafficUnits;
}
