package model.traffic;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "custom_erdi_units_generator")
    @SequenceGenerator(name = "custom_erdi_units_generator",
            schema = "portal", sequenceName = "custom_erdi_units_id_seq", allocationSize = 1)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    // cascade nothing, fetch eager
    @JoinColumn(name = "custom_erdi_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Include
    private CustomErdi customErdi;

    @NotNull
    @Column(nullable = false)
    //@Enumerated(EnumType.STRING)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String type;

    @NotNull
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String value;

}
