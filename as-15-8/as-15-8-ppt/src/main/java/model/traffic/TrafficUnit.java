package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.enums.TrafficUnitType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@MappedSuperclass
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class TrafficUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    //@JsonProperty(access = JsonProperty.Access.READ_ONLY) todo forbid name editing by user
    @ToString.Include
    private String name;

    public abstract void setTraffic(Traffic traffic);

    public abstract void syncContentAssociation();

    @JsonIgnore
    public abstract TrafficUnitType getType();

    @JsonIgnore
    public abstract boolean isEmpty();

    @JsonIgnore
    public final boolean nonEmpty() {
        return !isEmpty();
    }
}
