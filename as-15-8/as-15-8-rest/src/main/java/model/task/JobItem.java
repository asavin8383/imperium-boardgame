package model.task;

import lombok.Data;
import model.erdi.Content;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Проверка одной единицы ЕРДИ в рамках мероприятия
 */

@Entity
@Table(schema = "portal", name = "job_items")
@Data
public class JobItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_items_generator")
    @SequenceGenerator(name="job_items_generator", schema = "portal", sequenceName = "job_items_id_seq", allocationSize=1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="task_job_id", foreignKey = @ForeignKey(name = "FK_job_items_job_id"))
    private TaskJob taskJob;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id", foreignKey = @ForeignKey(name = "FK_job_items_content_id"))
    private Content content;
}
