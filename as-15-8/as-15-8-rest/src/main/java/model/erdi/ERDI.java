package model.erdi;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;
import model.Views;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import model.task.ArrangementItem;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Getter
@EqualsAndHashCode(of = {"id"})
public class ERDI {

    public enum CheckUnitType{
        URL("URL"),
        IP("IP"),
        DOMAIN("DOMAIN"),
        DOMAIN_MASK("DOMAIN-MASK")
        ;
        @Getter
        private String name;

        CheckUnitType(String name) {
            this.name = name;
        }
    }

    @Id
    @JsonView(Views.Id.class)
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


    public Optional<Map<String, String>> getFirstCheckUnit(){
        if (this.checkUnitType.equals(CheckUnitType.URL.getName())){
            return getExplicitCheckUnits(urls);
        } else if (this.checkUnitType.equals(CheckUnitType.IP.getName())){
            return getExplicitCheckUnits(ipList);
        } else if (this.checkUnitType.equals(CheckUnitType.DOMAIN.getName())){
            return getExplicitCheckUnits(domains);
        } else if (this.checkUnitType.equals(CheckUnitType.DOMAIN_MASK.getName())){
            return getExplicitCheckUnits(domainMasks);
        } else return Optional.empty();
    }

    private Optional<Map<String, String>> getExplicitCheckUnits(List<? extends CheckUnit> units){
        return units.stream()
                .map(unit -> {
                    Map<String, String> elem = new HashMap<>();
                    elem.put(unit.getCheckUnitType().toString(), unit.getCheckUnitValue());
                    return elem;
                }).findFirst();
    }

    @PostLoad
    void fillCheckUnitType(){
        if(blocktype==null){
            checkUnitType = CheckUnitType.URL.getName();
        } else {
            checkUnitType = blocktype.toUpperCase();
        }
    }
}
