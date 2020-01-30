package model.task;

import com.fasterxml.jackson.annotation.JsonView;
import enums.ExecutionStatus;
import lombok.Data;
import model.Views;
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
	@JsonView(Views.Id.class)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formal_tasks_generator")
	@SequenceGenerator(name="formal_tasks_generator", schema = "portal", sequenceName = "formal_tasks_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	/**Название*/
	@JsonView(Views.FormalTaskWithArrangement.class)
	@NotNull
	@Column(nullable = false)
	private String title;

	/**Оператор, ответственный за задание*/
	@Column(nullable = false)
	private String operator;

	/**Статус задания*/
	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
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
	/**Приоритет*/
	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Priority priority = Priority.MEDIUM;

	/**Ссылка на id неформализованного задания (поручения)*/
	private Long missionId;

	/**Ссылка на внешний id поручения*/
	private String fgisId;

	/**Список мероприятий по заданию*/
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "formalTask")
	//@JsonIgnore
	@JsonView(Views.FormalTaskWithArrangement.class)
	private List<Arrangement> arrangements = new ArrayList<>();
	
	public FormalTask() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.NEW;
	}

}
