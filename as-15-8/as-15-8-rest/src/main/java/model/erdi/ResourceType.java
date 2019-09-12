package model.erdi;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Immutable
@Table(schema = "sor", name = "resource_type")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResourceType implements Serializable {

    public static final Long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "dsc")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String description;

}
