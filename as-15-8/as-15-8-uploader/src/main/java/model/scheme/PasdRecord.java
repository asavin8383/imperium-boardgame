package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
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
public class PasdRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "pasd_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @ToString.Include
    private String name;

    @ToString.Include
    private String hostname;

    @Column(name = "domainnames")
    @ToString.Include
    private String domainNames;

    @Column(name = "servicedescription")
    @ToString.Include
    private String serviceDescription;

    @Column(name = "networkaddresses")
    @ToString.Include
    private String networkAddresses;

    @Column(name = "ipaccessfgis")
    @JsonIgnore
    @ToString.Include
    private String ipAccessFgis;

    @JsonIgnore
    @ToString.Include
    private String credentials;

    @ToString.Include
    private Integer origId;
    
    @ToString.Include
    private LocalDateTime ppnDt;

    @ToString.Include
    private LocalDateTime effDt;

    @ToString.Include
    private LocalDateTime cDate;

    public static Example<PasdRecord> example(ExampleMatcher matcher, LocalDateTime effDt,
                                              String name, String hostname) {
        PasdRecord pasd =  new PasdRecord();
        pasd.setEffDt(effDt);
        pasd.setName(name);
        pasd.setHostname(hostname);
        return Example.of(pasd, matcher);
    }

}
