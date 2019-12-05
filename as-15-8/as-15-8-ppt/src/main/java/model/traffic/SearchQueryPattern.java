package model.traffic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by san
 * Date: 25.11.2019
 */
@Entity
@Table(schema = "portal", name = "search_query_patterns")
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQueryPattern implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Id.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String name;

    @NotNull
    @Column(nullable = false)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String queryPattern;

    @ManyToMany
    @JoinTable(schema = "portal", name = "search_query_patterns_custom_erdi",
            joinColumns = @JoinColumn(name = "pattern_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_erdi_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<CustomErdi> customErdiList;

    @OneToMany(mappedBy = "searchQueryPattern",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<SearchQueryPatternContentJoin> formalErdiList;

    @ManyToMany
    @JoinTable(schema = "portal", name = "search_query_patterns_search_phrases",
            joinColumns = @JoinColumn(name = "pattern_id"),
            inverseJoinColumns = @JoinColumn(name = "search_phrase_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<SearchPhrase> searchPhrases;
}
