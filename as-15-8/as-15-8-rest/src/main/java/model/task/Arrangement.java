package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import exceptions.AS_15_8_Exception;
import lombok.Data;
import model.Views;
import model.catalog.AccessTool;
import enums.ExecutionStatus;
import stateMachine.ArrangementEvents;
import stateMachine.ArrangementStateMachine;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Duration;
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
	@JsonView(Views.Id.class)
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
	/**Запланированная дата запуска*/
	private LocalDateTime plannedDate;
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

    private ArrangementStateMachine stateMachine;
	
	public Arrangement() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.NEW;
		this.stateMachine = new ArrangementStateMachine(this.status);
	}

	public Long getDurationInMinutes(){
		if (this.startDate != null && this.endDate != null){
			return Duration.between(startDate, endDate).toMinutes();
		}else {
			return null;
		}
	}

	public void sendEvent(ArrangementEvents event){
		if(this.stateMachine.sendEvent(event)){
			this.status = stateMachine.getCurrentStatus();
		} else {
			throw new AS_15_8_Exception("Ошибка смены статуса мероприятия: " + stateMachine.getCurrentStatus() + " событием " + event);
		}
	}

	private void setStatus(ExecutionStatus status){
		this.status = status;
		this.stateMachine = new ArrangementStateMachine(status);
	}
}
