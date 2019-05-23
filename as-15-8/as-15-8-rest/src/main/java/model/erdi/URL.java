package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jobs.CheckUnitType;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@Entity
@Table(schema = "sa", name = "url")
@Immutable
@Getter
public class URL {

    @Id
    private Long id;

    private CheckUnitType checkUnitType = CheckUnitType.URL;

    @Column(name = "url")
    private String checkUnitValue;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private ERDI erdi;
}
