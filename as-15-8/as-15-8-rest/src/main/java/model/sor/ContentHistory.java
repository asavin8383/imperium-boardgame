package model.sor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(schema = "sor", name = "content_history")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    @JsonView(Views.Full.class)
    private FormalErdi content;

    @Column(name = "ppn_dt")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime creationDate;

    @Column(name = "st_dt")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime stDt;

    @Column(name = "end_dt")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private LocalDateTime endDate;

    @JsonIgnore
    @ToString.Include
    private Long contentVersionId;

    @JsonIgnore
    @ToString.Include
    private Long addonVersionId;

}
