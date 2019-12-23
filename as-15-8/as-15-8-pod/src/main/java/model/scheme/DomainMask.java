package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(schema = "sor", name = "domain_masks")
@Data
public class DomainMask implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String domainMask;

    @OneToMany(cascade = CascadeType.ALL,  mappedBy = "domainMask")
    @JsonIgnore
    private List<Domain> domains = new ArrayList<>();

    public int getDomainsNumber() {
        return domains.size();
    }
}
