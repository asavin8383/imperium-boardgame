package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

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

    @ToString.Include
    private LocalDateTime ppnDt;

    @ToString.Include
    private LocalDateTime effDt;

    @ToString.Include
    private LocalDateTime cDate;

    @ToString.Include
    @JsonIgnore
    private String origId;

    @ToString.Include
    private String registryName;

    @ToString.Include
    private String categoryName;

    @ToString.Include
    private String violationName;

    public static Example<Subtype> example(ExampleMatcher matcher, LocalDateTime effDt, String violationName) {
        Subtype v =  new Subtype();
        v.setEffDt(effDt);
        v.setViolationName(violationName);
        return Example.of(v, matcher);
    }

}
