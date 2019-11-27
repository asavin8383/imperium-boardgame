package model.scheme;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @JsonFormat(timezone = "GMT+03:00")
    @ToString.Include
    private Date ppnDt;

    @JsonIgnore
//    @JsonFormat(timezone = "GMT+03:00")
    @ToString.Include
    private Date effDt;

    @Column(name = "c_date")
    @JsonProperty("cDate")
    @JsonFormat(timezone = "GMT+03:00")
    @ToString.Include
    private Date cDate;

    @Transient
    private String source = "ППП Реестр анонимайзеров";

}
