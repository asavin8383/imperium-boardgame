package model.task;

import lombok.Data;
import model.erdi.ERDI;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Проверка одной единицы ЕРДИ в рамках мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_items")
@Data
public class ArrangementItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="arrangement_items_generator")
    @SequenceGenerator(name="arrangement_items_generator", schema="portal", sequenceName="arrangement_items_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="arrangement_id", foreignKey = @ForeignKey(name = "FK_arrangement_items_arrangement_id"))
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id", foreignKey = @ForeignKey(name = "FK_arrangement_items_content_id"))
    private ERDI erdi;
}
