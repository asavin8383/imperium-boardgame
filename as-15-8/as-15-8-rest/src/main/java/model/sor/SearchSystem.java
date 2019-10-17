package model.sor;

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
@Table(schema = "sor", name = "pslist")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SearchSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ps_id")
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



    public static Example<SearchSystem> example(ExampleMatcher matcher, String name,
                                                String hostname, LocalDateTime effDt) {
        SearchSystem v =  new SearchSystem(null, name, hostname,
                null, null, effDt, null);
        return Example.of(v, matcher);
    }

}
