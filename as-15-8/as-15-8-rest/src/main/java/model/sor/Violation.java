package model.sor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Immutable
@Table(schema = "sor", name = "violation")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Violation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String fullId;

    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String name;

    @Column(nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Boolean deleted;

    /*@Column(nullable = false)
    private LocalDateTime updateTime;*/

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonView(Views.Full.class)
    private Violation parent;

    @OneToMany(mappedBy = "parent",
            cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JsonIgnore
    private List<Violation> children;



    public static Example<Violation> example(ExampleMatcher matcher, String name, Boolean deleted) {
        Violation v =  new Violation(null, null, name, deleted, null, null);
        return Example.of(v, matcher);
    }

}
