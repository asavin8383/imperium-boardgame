package model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.erdi.ERDI;
import model.task.Arrangement;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "portal", name = "arrangement_results")
@Data
public class ArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="job_results_generator")
    @SequenceGenerator(name="job_results_generator", schema="portal", sequenceName="job_results_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="task_job_id", foreignKey = @ForeignKey(name = "FK_job_results_job_id"))
    @JsonIgnore
    private Arrangement arrangement;

    @ManyToOne(optional = false)
    @JoinColumn(name="content_id", foreignKey = @ForeignKey(name = "FK_job_results_content_id"))
    @JsonIgnore
    private ERDI ERDI;

    /**URL проверенного ресурса*/
    private String url;

    /**Результат проверки*/
    private String result;

    @Lob
    @Column(name="screenshot", columnDefinition="bytea")
    private byte[] screenshot;


}
