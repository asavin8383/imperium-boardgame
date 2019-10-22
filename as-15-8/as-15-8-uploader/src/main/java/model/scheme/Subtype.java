package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(schema = "sor", name = "subtype")
@Data
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subtype implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "subtype_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subtype_generator")
    @SequenceGenerator(name = "subtype_generator",
            schema = "sor", sequenceName = "subtype_subtype_id_seq", allocationSize = 1)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    // Дата изменения записи АС 15.8
    // Дата и время получения сведений - max(ppn_dt)
    @ToString.Include
    @JsonIgnore
    private LocalDateTime ppnDt;

    @ToString.Include
    @JsonIgnore
    private LocalDateTime effDt;

    // Дата и время последнего изменения - max(cdate)
    @Column(name = "c_date")
    @JsonProperty("cDate")
    @ToString.Include
    private LocalDateTime crDate;

    @ToString.Include
    private String origId;

    @ToString.Include
    private String registryName;

    @ToString.Include
    private String categoryName;

    @ToString.Include
    private String violationName;

//    to do js - violation composite id
//    public String getCompositeId()

}
