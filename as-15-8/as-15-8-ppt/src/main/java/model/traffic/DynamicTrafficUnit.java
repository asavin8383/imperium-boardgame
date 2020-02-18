package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.enums.TrafficUnitType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "portal", name = "dynamic_traffic_unit")
@PrimaryKeyJoinColumn(name = "traffic_unit_id", referencedColumnName = "id")
@OnDelete(action = OnDeleteAction.CASCADE)
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicTrafficUnit extends TrafficUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "traffic_id", nullable = false)
    @JsonIgnore
    @ToString.Include
    private Traffic traffic;

    @Column
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String query;

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
               StringUtils.isEmpty(getName()) &&
               StringUtils.isEmpty(query);
    }
}

