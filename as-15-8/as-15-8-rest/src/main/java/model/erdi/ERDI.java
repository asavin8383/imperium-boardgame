package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import model.task.ArrangementItem;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ERDI {
    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "erdi")
    @JsonIgnore
    private List<ArrangementItem> arrangementItems;

    @OneToMany(mappedBy = "erdi")
    private List<Decision> decisionList;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<Domain> domains;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<DomainMask> domainMasks;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<URL> urls;

    @OneToMany(mappedBy = "erdi")
    @JsonIgnore
    private List<IP> ipList;
}
