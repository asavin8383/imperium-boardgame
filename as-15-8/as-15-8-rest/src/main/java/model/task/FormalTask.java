package model.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.user.User;

/**
 * Формализованное задание на проведение мероприятий по контролю
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="formal_tasks")
@Data
public class FormalTask implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formal_tasks_generator")
	@SequenceGenerator(name="formal_tasks_generator", schema = "portal", sequenceName = "formal_tasks_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@NotNull
	/**Название*/
	private String title;
	
	@ManyToOne(optional=false)
	@JsonIgnore
	/**Автор задания (оператор)*/
	private User author;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	/**Статус задания*/
	private TaskOrJobStatus status;
	
	/**Дата создания*/
	private Date creationDate;
	/**Дата начала*/
	private Date startDate;
	/**Дата окончания*/
	private Date endDate;
	/**Дата последнего изменения*/
	private Date modificationDate;
	
	@ManyToOne(optional=false)
	@JsonIgnore
	/**Ссылка на неформализованное задание*/
	private InformalTask informalTask;
	
	@OneToMany(cascade=CascadeType.ALL,mappedBy="formalTask")
	@JsonIgnore
	/**Список мероприятий по заданию*/
	private List<TaskJob> taskJobs;
	
	public FormalTask() {
		this.creationDate = new Date();
		this.status = TaskOrJobStatus.PLANNED;
	}
}
