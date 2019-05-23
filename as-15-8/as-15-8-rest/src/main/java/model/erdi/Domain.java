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
@Subselect("select domain.id, domain from sa.domain join sa.content on domain.content_id=content.id and upper(content.blocktype) = 'DOMAIN'")
@Immutable
@Getter
public class Domain {

    @Id
    private Long id;

    private CheckUnitType checkUnitType = CheckUnitType.DOMAIN;

    @Column(name = "domain")
    private String checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private ERDI erdi;

}
