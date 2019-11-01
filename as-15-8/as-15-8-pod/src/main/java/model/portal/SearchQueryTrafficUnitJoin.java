package model.portal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import model.projection.ContentView;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Immutable
@Table(schema = "portal", name = "search_query_traffic_units_content")
@Data
@Setter(AccessLevel.PRIVATE)
public class SearchQueryTrafficUnitJoin {

    @Id
    private Long id;

    private Long trafficUnitId;

    @ManyToOne
    @JoinColumn(name = "content_id", referencedColumnName = "content_id")
    private ContentView contentView;

}
