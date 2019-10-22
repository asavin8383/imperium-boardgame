package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
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
    @ToString.Include
    private String ipAccessFgis;

    @ToString.Include
    private String credentials;

    @ToString.Include
    @JsonIgnore
    private Integer origId;
    
    @ToString.Include
    private LocalDateTime ppnDt;

    @ToString.Include
    @JsonIgnore
    private LocalDateTime effDt;

    @Column(name = "c_date")
    @JsonProperty("cDate")
    @ToString.Include
    private LocalDateTime crDate;

    @Transient
    private String status = "Работает";

    @Transient
    private String source = "ППП Реестр анонимайзеров";

}
