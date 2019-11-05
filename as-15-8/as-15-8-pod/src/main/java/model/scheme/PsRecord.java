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
@Table(schema = "sor", name = "ps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ps_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @ToString.Include
    private String name;

    @ToString.Include
    private String hostname;

    @ToString.Include
    @JsonIgnore
    private Integer origId;

    @JsonIgnore
//    @JsonFormat(timezone = "GMT+03:00")
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
