package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.catalog.AccessTool;
import model.enums.ExecutionStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**Мероприятие в рамках формализованного задания*/

@Entity
@Table(schema="portal", name="arrangements",
	uniqueConstraints = @UniqueConstraint(name = "uq_arrangements_formal_task_id_access_tool_id", columnNames = {"formal_task_id", "access_tool_id"}))
@Data
public class Arrangement implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="arrangements_generator")
	@SequenceGenerator(name="arrangements_generator", schema="portal", sequenceName="arrangements_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	/**Название мероприятия*/
	@NotNull
	private String title;

	/**Статус мероприятия*/
	@Enumerated(EnumType.STRING)
	@NotNull
	private ExecutionStatus status;
	
	/**Дата создания*/
	private LocalDateTime creationDate;
	/**Дата начала*/
	private LocalDateTime startDate;
	/**Дата окончания*/
	private LocalDateTime endDate;

	/**Формализованное задание на проведение мероприятий*/
	@ManyToOne(optional=false)
	@JoinColumn(name="formal_task_id", foreignKey = @ForeignKey(name = "FK_arrangements_formal_task_id"))
	@JsonIgnore
	private FormalTask formalTask;

	/**Список поисковых систем для проверки*/
	@ManyToOne(optional = false)
	@JoinColumn(name="access_tool_id", foreignKey = @ForeignKey(name = "FK_arrangements_access_tool_id"))
	private AccessTool accessTool;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement", fetch = FetchType.EAGER)
	@JsonIgnore
	private List<ArrangementItem> arrangementItems;

	/**Результат проведения мероприятия*/
    private String result;
	
	public Arrangement() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.NEW;
	}
}
