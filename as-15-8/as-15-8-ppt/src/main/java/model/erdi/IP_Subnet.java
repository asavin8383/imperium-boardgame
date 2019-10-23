package model.erdi;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

/**
 * Creation date: 15.06.2019
 * Author: asavin
 */
@Entity
@Table(schema = "sa", name = "ipsubnet")
@Immutable
@Getter
public class IP_Subnet implements CheckUnit{
    @Id
    private Long id;

    @Transient
    private CheckUnitType checkUnitType = CheckUnitType.IP_V4_SUBNET;

    @Column(name = "ipsubnet")
    private String checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private ERDI erdi;
}
