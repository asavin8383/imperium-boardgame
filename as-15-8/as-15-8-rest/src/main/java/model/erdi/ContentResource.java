package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(schema = "sor", name = "content_resources")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    @JsonIgnore
    private FormalErdi content;

    @NotNull
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String value;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime ts;

    @ManyToOne
    @JoinColumn(name = "resource_type_id")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private ResourceType resourceType;

    @JsonIgnore
    @ToString.Include
    private Long contentVersionId;

}
