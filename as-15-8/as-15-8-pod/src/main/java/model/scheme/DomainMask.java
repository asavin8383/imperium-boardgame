package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "sor", name = "domain_masks")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DomainMask implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private String domainMask;

    @OneToMany(cascade = CascadeType.ALL,  mappedBy = "domainMask")
    private List<Domain> domains = new ArrayList<>();

    public int getDomainsNumber() {
        return domains.size();
    }
}
