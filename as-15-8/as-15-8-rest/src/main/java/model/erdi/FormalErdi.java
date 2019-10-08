package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import model.traffic.ErdiTrafficUnit;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Immutable
@Table(schema = "sor", name = "content")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FormalErdi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String erdiId;

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
    @JsonView(Views.Full.class)
    private List<ContentResource> contentResources;

    @JsonIgnore
    @ToString.Include
    private Long initContentVersionId;

    @ManyToMany(mappedBy = "formalErdiList", cascade = CascadeType.REFRESH)
    @JsonIgnore
    private List<ErdiTrafficUnit> erdiTrafficUnits;

}
