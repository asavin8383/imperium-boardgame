package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.task.JobItem;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "sa", name = "content")
@Immutable
@Data
public class Content {
    @Id
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "content")
    @JsonIgnore
    private List<JobItem> jobItems;
}
