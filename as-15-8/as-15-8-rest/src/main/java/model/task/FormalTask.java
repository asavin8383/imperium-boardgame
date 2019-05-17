package model.task;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.enums.ExecutionStatus;
import model.enums.Priority;
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
	/**Оператор, ответственный за задание*/
	private User user;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	/**Статус задания*/
	private ExecutionStatus status;
	
	/**Дата создания*/
	private LocalDateTime creationDate;
	/**Дата начала*/
	private LocalDateTime startDate;
	/**Дата окончания*/
	private LocalDateTime endDate;
	/**Дата последнего изменения*/
	private LocalDateTime modificationDate;
	/**Срок дедлайна*/
	private LocalDateTime deadlineDate;
	/**Автор задания из ФГИС*/
	private String author;
	/**Признак согласования*/
	private boolean agreed;
	/**Идентификатор ФГИС*/
	private String fgisId;
	/**Приоритет*/
	@Enumerated(EnumType.STRING)
	private Priority priority;
	
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
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.PLANNED;
	}
}
