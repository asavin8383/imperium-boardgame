package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import enums.SortingDirection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.converters.DynamicTrafficConverter;
import model.enums.TrafficUnitType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(schema = "portal", name = "dynamic_traffic_unit")
@PrimaryKeyJoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
@OnDelete(action = OnDeleteAction.CASCADE)
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicTrafficUnit extends TrafficUnit implements Serializable {

    private static final long serialVersionUID = -767565436097612605L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "traffic_id", nullable = false)
    @JsonIgnore
    private Traffic traffic;

    @Column
    @JsonView(Views.Brief.class)
    private String idMask;

    @Column
    @JsonView(Views.Brief.class)
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> categoryNames;

    @Column
    @JsonView(Views.Brief.class)
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> decisionOrgs;

    @Column
    @JsonView(Views.Brief.class)
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> infoTypeIds;

    @Column
    @JsonView(Views.Brief.class)
    @ToString.Include
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> registryNames;

    @Column
    @JsonView(Views.Brief.class)
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> resourceTypes;

    @Column
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String resourceValue;

    @Column
    @JsonView(Views.Brief.class)
    @Convert(converter = DynamicTrafficConverter.class)
    private List<String> violationNames;

    @Column
    @JsonView(Views.Brief.class)
    private Integer size;

    @Column
    @JsonView(Views.Brief.class)
    private LocalDate startTime;

    @Column
    @JsonView(Views.Brief.class)
    private LocalDate endTime;

    @Column
    @JsonView(Views.Brief.class)
    private  Boolean random;

    @Column
    @JsonView(Views.Brief.class)
    @Enumerated(EnumType.STRING)
    private SortingDirection sortingDirection;

    @Column
    @JsonView(Views.Brief.class)
    private String sortingColumn;

    @Column
    @JsonView(Views.Brief.class)
    private Long visitorsCntRussiaMin;

    @Column
    @JsonView(Views.Brief.class)
    private Long visitorsCntRussiaMax;

    @Column
    @JsonView(Views.Brief.class)
    private Long visitorsCntWorldMin;

    @Column
    @JsonView(Views.Brief.class)
    private Long visitorsCntWorldMax;

    @Column
    @JsonView(Views.Brief.class)
    private Long erdiCountAbout = 0L;

    @Override
    public void syncContentAssociation() {

    }

    @Override
    public TrafficUnitType getType() {
        if (isEmpty()) throw new IllegalStateException(
                "Cannot infer type for empty TrafficUnit");
        return TrafficUnitType.DYNAMIC;
    }

    @Override
    public boolean isEmpty() {
       return  getId() == null && category == null &&
               StringUtils.isEmpty(getName());
    }

}

