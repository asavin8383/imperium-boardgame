package model.sor;

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
@Table(schema = "sor", name = "blocktype")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BlockType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "blocktype")
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String blockType;

    @Column(name = "dsc")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String description;
}
