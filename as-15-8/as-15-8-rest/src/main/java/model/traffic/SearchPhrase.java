package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.sor.Violation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "search_phrases")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchPhrase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_phrase_generator")
    @SequenceGenerator(name = "search_phrase_generator",
            schema = "portal", sequenceName = "search_phrases_id_seq", allocationSize = 1)
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String phrase;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "violation_id")
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Violation violation;

    @ManyToMany(mappedBy = "searchPhrases", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SearchQueryTrafficUnit> trafficUnits;



    @Transient
    private Boolean checked;

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

}
