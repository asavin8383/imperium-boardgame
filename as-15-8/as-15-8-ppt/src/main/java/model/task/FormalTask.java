package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.ExecutionStatus;
import lombok.Data;
import model.enums.Priority;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	/**Название*/
	@NotNull
	private String title;

	/**Оператор, ответственный за задание*/
	@Column(nullable = false)
	private String operator;

	/**Статус задания*/
	@Enumerated(EnumType.STRING)
	@NotNull
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

	/**Ссылка на неформализованное задание*/
	@ManyToOne
	@JoinColumn(name="informal_task_id", foreignKey = @ForeignKey(name = "FK_formal_tasks_informal_task_id"))
	private InformalTask informalTask;

	/**Список мероприятий по заданию*/
	@OneToMany(cascade=CascadeType.ALL,mappedBy="formalTask")
	@JsonIgnore
	private List<Arrangement> arrangements = new ArrayList<>();
	
	public FormalTask() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.FORMED;
	}

}
