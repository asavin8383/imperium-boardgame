package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "sor", name = "content")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FormalErdi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_generator")
    @SequenceGenerator(name = "content_generator",
            schema = "sor", sequenceName = "content_id_seq", allocationSize = 1)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String erdiId;

    // @JoinColumn(foreignKey = @ForeignKey(name = "content_version_fk"))
    @JsonIgnore
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long initContentVersionId;

}
