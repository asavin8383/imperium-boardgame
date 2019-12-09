package model.scheme;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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

    @ToString.Include
    private String origId;

    @ToString.Include
    private String registryName;

    @ToString.Include
    private String categoryName;

    @ToString.Include
    private String violationName;

    public String getShortName(){
        String result = "";
        if(!Strings.isNullOrEmpty(registryName)) {
            result += this.registryName + "/";
        }
        if(!Strings.isNullOrEmpty(categoryName)) {
            result += this.categoryName + "/";
        }
        if(!Strings.isNullOrEmpty(violationName)) {
            result += this.violationName;
        }
        return result;
    }

}
