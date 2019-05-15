package model.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
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
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
	private Long id;
	
	@NotNull
	/**Название*/
	private String title;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "FK_formal_tasks_user_id"))
	@JsonIgnore
	/**Автор задания (оператор)*/
	private User author;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	/**Статус задания*/
	private ExecutionStatus status;
	
	/**Дата создания*/
	private Date creationDate;
	/**Дата начала*/
	private Date startDate;
	/**Дата окончания*/
	private Date endDate;
	/**Дата последнего изменения*/
	private Date modificationDate;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="informal_task_id", foreignKey = @ForeignKey(name = "FK_formal_tasks_informal_task_id"))
	@JsonIgnore
	/**Ссылка на неформализованное задание*/
	private InformalTask informalTask;
	
	@OneToMany(cascade=CascadeType.ALL,mappedBy="formalTask")
	@JsonIgnore
	/**Список мероприятий по заданию*/
	private List<Arrangement> arrangements;
	
	public FormalTask() {
		this.creationDate = new Date();
		this.status = ExecutionStatus.PLANNED;
	}
}
