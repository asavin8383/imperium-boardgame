package model.erdi;

import jobs.CheckUnitType;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */

@Entity
@Subselect("select domain.id, domain from sa.domain join sa.content on domain.content_id=content.id and upper(content.blocktype) = 'DOMAIN-MASK'")
@Immutable
@Data
public class DomainMask {

    @Id
    private Long id;

    private CheckUnitType checkUnitType = CheckUnitType.DOMAIN_MASK;

    @Column(name = "domain")
    private String checkUnitValue;
}
