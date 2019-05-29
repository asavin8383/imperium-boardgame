package model.erdi;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@Entity
@Table(schema = "sa", name = "ip")
@Immutable
@Getter
public class IP {

    @Id
    private Long id;

    @Transient
    private CheckUnitType checkUnitType = CheckUnitType.URL;

    @Column(name = "ip")
    private String checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private ERDI erdi;
}
