package model.erdi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.fasterxml.jackson.annotation.JsonView;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import model.Views;
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
        DOMAIN_MASK("DOMAIN-MASK"),
        IPSUBNET("IPSUBNET")
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

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<IP_Subnet> ip_subnetList;


    public Optional<Map<String, String>> getFirstCheckUnit(){
        if (this.checkUnitType.equals(CheckUnitType.URL.getName())){
            return getExplicitCheckUnits(urls);
        } else if (this.checkUnitType.equals(CheckUnitType.IP.getName())){
            return getExplicitCheckUnits(ipList);
        } else if (this.checkUnitType.equals(CheckUnitType.DOMAIN.getName())){
            return getExplicitCheckUnits(domains);
        } else if (this.checkUnitType.equals(CheckUnitType.DOMAIN_MASK.getName())){
            return getExplicitCheckUnits(domainMasks);
        } else if (this.checkUnitType.equals(CheckUnitType.IPSUBNET.getName())){
            return getExplicitCheckUnits(ip_subnetList);
        }
        else return Optional.empty();
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
        //TODO Всё это - костыли. После испытаний разобраться. В content нужно заводить поле, чётко определяющее тип
        if(blocktype==null){
            checkUnitType = CheckUnitType.URL.getName();
        } else if (blocktype.toUpperCase().equals(CheckUnitType.IP.getName())){
            if (this.ip_subnetList!=null && this.ip_subnetList.size()>0){
                checkUnitType = CheckUnitType.IPSUBNET.getName();
            } else {
                checkUnitType = CheckUnitType.IP.getName();
            }
        }
        else {
            checkUnitType = blocktype.toUpperCase();
        }
    }
}
