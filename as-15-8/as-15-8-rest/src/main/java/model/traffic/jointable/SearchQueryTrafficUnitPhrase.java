package model.traffic.jointable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(schema = "portal", name = "search_query_traffic_units_search_phrases")
@Data
@Setter(AccessLevel.PRIVATE)
public class SearchQueryTrafficUnitPhrase {

    @Id
    private Long id;

    @Column(name = "traffic_unit_id")
    private Long trafficUnitId;

    @Column(name = "search_phrase_id")
    private Long searchPhraseId;

}
