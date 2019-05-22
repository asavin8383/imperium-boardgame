package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.task.ArrangementItem;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Data
public class ERDI {
    @Id
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "erdi")
    @JsonIgnore
    private List<ArrangementItem> arrangementItems;
}
