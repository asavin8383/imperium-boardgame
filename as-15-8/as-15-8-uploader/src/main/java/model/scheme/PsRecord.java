package model.scheme;

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
@Table(schema = "sor", name = "ps")
@Data
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ps_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @ToString.Include
    private String name;
    
    @ToString.Include
    private String hostname;

    @ToString.Include
    private Integer origId;

    @ToString.Include
    private LocalDateTime ppnDt;

    @ToString.Include
    private LocalDateTime effDt;

    @ToString.Include
    private LocalDateTime cDate;

    public static Example<PsRecord> example(ExampleMatcher matcher, String name,
                                            String hostname, LocalDateTime effDt) {
        PsRecord v = new PsRecord();
        v.setEffDt(effDt);
        v.setName(name);
        v.setHostname(hostname);
        return Example.of(v, matcher);
    }

}
