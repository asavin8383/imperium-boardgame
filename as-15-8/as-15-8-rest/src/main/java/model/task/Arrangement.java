package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import model.catalog.AccessTool;
import model.enums.ExecutionStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**Мероприятие в рамках формализованного задания*/

//TODO Проверять уникальность ПС/ПАСД в рамках одного задания - исправить в БД

@Entity
@Table(schema="portal", name="arrangements")
@Data
public class Arrangement implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
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
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private FormalTask formalTask;

	/**Список поисковых систем для проверки*/
	@ManyToOne(optional = false)
	@JoinColumn(name="access_tool_id", foreignKey = @ForeignKey(name = "FK_arrangements_access_tool_id"))
	@JsonIgnore
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private AccessTool accessTool;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement", fetch = FetchType.EAGER)
	private List<ArrangementItem> arrangementItems;

	/**Записи об исполнении мероприятия*/
	@OneToMany(mappedBy = "arrangement", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<ArrangementExecution> arrangementExecutionList;
	
	/**Результат проведения мероприятия*/
    private String result;
	
	public Arrangement() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.PLANNED;
	}

	@JsonIgnore
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public Map<String, Object> getArrangementInfo(){
		Map<String, Object> result = new HashMap<>();
		result.put("id", this.id);
		result.put("title", this.title);
		result.put("status", this.status);
		result.put("creationDate", this.creationDate);
		result.put("endDate", this.endDate);
		result.put("accessTool", this.accessTool.getName());
		result.put("result", this.result);

		return result;
	}
}
