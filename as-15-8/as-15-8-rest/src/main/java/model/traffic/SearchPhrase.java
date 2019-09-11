package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "search_phrases")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Long violationId;



    @ManyToMany(mappedBy = "searchPhrases")
    // cascade nothing, fetch lazy
    @JsonIgnore
    private List<SearchQueryTrafficUnit> trafficUnits;

}
