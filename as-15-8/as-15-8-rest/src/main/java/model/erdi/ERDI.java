package model.erdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import model.task.ArrangementItem;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ERDI {

    private static final String URL = "URL";
    private static final String IP = "IP";
    private static final String DOMAIN = "DOMAIN";
    private static final String DOMAIN_MASK = "DOMAIN-MASK";
    private static final Integer LIMIT = 10;

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    //TODO Плохо!!! Нужно по-человечески разобраться
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
    @JsonIgnore
    private List<Domain> domains;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<DomainMask> domainMasks;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<URL> urls;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<IP> ipList;

    public List<Map<String, String>> getCheckUnits(){
        if (this.checkUnitType.equals(URL)){
            return getExplicitCheckUnits(urls);
        } else if (this.checkUnitType.equals(IP)){
            return getExplicitCheckUnits(ipList);
        } else if (this.checkUnitType.equals(DOMAIN)){
            return getExplicitCheckUnits(domains);
        } else if (this.checkUnitType.equals(DOMAIN_MASK)){
            return getExplicitCheckUnits(domainMasks);
        } else return new ArrayList<>();
    }

    private List<Map<String, String>> getExplicitCheckUnits(List<? extends CheckUnit> units){
        return units.stream().limit(LIMIT)
                .map(unit -> {
                    Map<String, String> elem = new HashMap<>();
                    elem.put(unit.getCheckUnitType().toString(), unit.getCheckUnitValue());
                    return elem;
                }).collect(Collectors.toList());
    }

    @PostLoad
    void fillCheckUnitType(){
        if(blocktype==null){
            checkUnitType = URL;
        } else {
            checkUnitType = blocktype.toUpperCase();
        }
    }
}
