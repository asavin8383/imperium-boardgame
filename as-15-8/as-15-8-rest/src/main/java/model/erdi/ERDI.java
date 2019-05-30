package model.erdi;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import model.task.ArrangementItem;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ERDI {

    private static final String defaultType = "URL";

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Basic
    @JsonIgnore
    private String blocktype;

    @Transient
    private String checkUnitType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "erdi")
    @JsonIgnore
    private List<ArrangementItem> arrangementItems;

    @OneToMany(mappedBy = "erdi")
    private List<Decision> decisionList;

    @OneToMany(mappedBy = "erdi")
    private List<Domain> domains;

    @OneToMany(mappedBy = "erdi")
    private List<DomainMask> domainMasks;

    @OneToMany(mappedBy = "erdi")
    private List<URL> urls;

    @OneToMany(mappedBy = "erdi")
    private List<IP> ipList;

    @PostLoad
    void fillCheckUnitType(){
        if(blocktype==null){
            checkUnitType = defaultType;
        } else {
            checkUnitType = blocktype.toUpperCase();
        }
    }
}
