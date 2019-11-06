package model.traffic;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Table(schema = "portal", name = "search_query_traffic_units_search_phrases")
@Data
@Setter(AccessLevel.PRIVATE)
public class SearchQueryTrafficUnitPhrase implements Serializable {

    private static final long serialVersionUID = 8013138178354941443L;
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
    private SearchQueryTrafficUnit trafficUnit;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "search_phrase_id", referencedColumnName = "id")
    private SearchPhrase searchPhrase;
}
