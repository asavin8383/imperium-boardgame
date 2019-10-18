package model.sor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(schema = "sor", name = "pasd")
@Data
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pasd implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "pasd_id")
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private String name;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private String hostname;

    @Column(name = "domainnames")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String domainNames;

    @Column(name = "servicedescription")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String serviceDescription;

    @Column(name = "networkaddresses")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String networkAddresses;

    @Column(name = "ipaccessfgis")
    @JsonIgnore
    @ToString.Include
    private String ipAccessFgis;

    @JsonIgnore
    @ToString.Include
    private String credentials;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private Integer origId;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime ppnDt;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime effDt;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime cDate;



    public static Example<Pasd> example(ExampleMatcher matcher, LocalDateTime effDt,
                                        String name, String hostname) {
        return example(matcher, effDt, name, hostname, null, null);
    }

    public static Example<Pasd> example(ExampleMatcher matcher, LocalDateTime effDt,
                                        String name, String hostname,
                                        String domainNames, String networkAddresses) {
        Pasd pasd =  new Pasd();
        pasd.setEffDt(effDt);
        pasd.setName(name);
        pasd.setHostname(hostname);
        pasd.setDomainNames(domainNames);
        pasd.setNetworkAddresses(networkAddresses);
        return Example.of(pasd, matcher);
    }

}
