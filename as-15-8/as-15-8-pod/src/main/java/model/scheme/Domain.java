package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(schema = "sor", name = "domains", uniqueConstraints = @UniqueConstraint(columnNames = { "domain","domain_mask_id"}))
@Data
public class Domain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String domain;

    @ManyToOne(optional = false)
    @JoinColumn(name = "domain_mask_id", foreignKey = @ForeignKey(name = "FK_domain_mask_id"))
    @JsonIgnore
    private DomainMask domainMask;

}
