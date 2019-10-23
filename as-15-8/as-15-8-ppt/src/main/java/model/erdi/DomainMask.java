package model.erdi;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.*;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */

@Entity
@Subselect("select * from sa.domain where domain like '%*%'")
@Immutable
@Getter
public class DomainMask implements CheckUnit{

    @Id
    private Long id;

    @Transient
    private CheckUnitType checkUnitType = CheckUnitType.DOMAIN_MASK;

    @Column(name = "domain")
    private String checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private ERDI erdi;
}
