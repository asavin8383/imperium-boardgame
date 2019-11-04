package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "portal", name = "custom_erdi_units")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomErdiUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Type type;

    @NotNull
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String value;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_erdi_id", nullable = false)
    @JsonIgnore
    private CustomErdi customErdi;

    public enum Type {
        URL, DOMAIN, DOMAIN_MASK, IP_V4, IP_V6, IP_V4_SUBNET, IP_V6_SUBNET
    }

}
