package model.traffic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.catalog.AccessToolsCategory;
import model.enums.TrafficUnitType;
import model.erdi.FormalErdi;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import utils.TrafficUnitUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "portal", name = "search_query_traffic_units")
@PrimaryKeyJoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQueryTrafficUnit extends TrafficUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonView(Views.Brief.class)
    private AccessToolsCategory category;

    @JsonView(Views.Brief.class)
    @ToString.Include
    private String queryPattern;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(schema = "portal", name = "search_query_traffic_units_custom_erdi",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "custom_erdi_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<CustomErdi> customErdiList;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(schema = "portal", name = "search_query_traffic_units_content",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<FormalErdi> formalErdiList;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(schema = "portal", name = "search_query_traffic_units_search_phrases",
            joinColumns = @JoinColumn(name = "traffic_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "search_phrase_id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<SearchPhrase> searchPhrases;

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(getName()) && category == null &&
                StringUtils.isEmpty(queryPattern) &&
                CollectionUtils.isEmpty(customErdiList) &&
                CollectionUtils.isEmpty(formalErdiList) &&
                CollectionUtils.isEmpty(searchPhrases);
    }

    @Override
    public TrafficUnitType getType() {
        return isEmpty() ? null : StringUtils.isEmpty(getName()) ?
                (StringUtils.isEmpty(queryPattern) ?
                        TrafficUnitType.PHRASE : TrafficUnitType.TEMPLATE) :
                TrafficUnitUtils.getType(this);
    }

}
