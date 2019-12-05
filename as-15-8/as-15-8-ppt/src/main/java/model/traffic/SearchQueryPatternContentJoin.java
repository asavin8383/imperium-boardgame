package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(schema = "portal", name = "search_query_patterns_content")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SearchQueryPatternContentJoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pattern_id", referencedColumnName = "id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private SearchQueryPattern searchQueryPattern;

    // для единообразия json при сохранении трафика
    @JsonProperty("id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long contentId;

}
