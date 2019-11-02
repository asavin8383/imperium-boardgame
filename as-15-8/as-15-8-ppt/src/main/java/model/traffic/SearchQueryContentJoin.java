package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(schema = "portal", name = "search_query_traffic_units_content")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SearchQueryContentJoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    @ToString.Include
    private SearchQueryTrafficUnit trafficUnit;

    // для единообразия json при сохранении трафика
    @JsonProperty("id")
    @ToString.Include
    private Long contentId;

}
