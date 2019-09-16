package model.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Table(schema = "sor", name = "violation")
@Data
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
    private boolean deleted;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "parent_id")
    @JsonView(Views.Full.class)
    private Violation parent;

}
