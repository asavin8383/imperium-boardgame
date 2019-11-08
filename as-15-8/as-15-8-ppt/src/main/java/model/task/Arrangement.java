package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import enums.ArrangementEvents;
import enums.ExecutionStatus;
import exceptions.AS_15_8_PPT_Exception;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;
import model.result.ArrangementResult;
import model.traffic.Traffic;
import stateMachine.ArrangementStateMachine;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**Мероприятие в рамках формализованного задания*/

@Entity
@Table(schema="portal", name="arrangements")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Arrangement implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="arrangements_generator")
	@SequenceGenerator(name="arrangements_generator", schema="portal", sequenceName="arrangements_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	@JsonView(Views.Id.class)
	@ToString.Include
	@EqualsAndHashCode.Include
	private Long id;

	/**Название мероприятия*/
	@NotNull
	@ToString.Include
	@EqualsAndHashCode.Include
	@JsonView(Views.Brief.class)
	private String title;

	/**Статус мероприятия*/
	@Enumerated(EnumType.STRING)
	@NotNull
	@ToString.Include
	@JsonView(Views.Brief.class)
	private ExecutionStatus status;
	
	/**Дата создания*/
	@ToString.Include
	@JsonView(Views.Brief.class)
	private LocalDateTime creationDate;

	/** Дата завершения */
	@JsonView(Views.Brief.class)
	private LocalDate completionDate;

	/**Плановая дата начала*/
	@JsonView(Views.Brief.class)
	private LocalTime plannedStartTime;
	/**Плановая дата окончания*/
	@JsonView(Views.Brief.class)
	private LocalTime plannedEndTime;

	/**Дата начала*/
	@JsonView(Views.Brief.class)
	private LocalTime startTime;
	/**Дата окончания*/
	@JsonView(Views.Brief.class)
	private LocalTime endTime;

	/** Максимальное количество обработчиков мероприятия */
	@JsonView(Views.Brief.class)
	private Integer maxWorkersCount;

	/** ПС/ПАСД мероприятия*/
	@JsonView(Views.Brief.class)
	@ToString.Include
	@NotNull
	private String accessTool;

	/**Формализованное задание на проведение мероприятий*/
	@ManyToOne(optional=false)
	@JoinColumn(name="formal_task_id", foreignKey = @ForeignKey(name = "FK_arrangements_formal_task_id"))
	@JsonIgnore
	private FormalTask formalTask;

	@OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement")
	@JsonIgnore
	private List<ArrangementResult> arrangementResults;

	@ManyToOne
	@JoinColumn(name = "traffic_id")
	private Traffic traffic;

	/**Результат проведения мероприятия*/
	@JsonView(Views.Brief.class)
    private String result;

    @Transient
    private ArrangementStateMachine stateMachine;
	
	public Arrangement() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.NEW;
		this.stateMachine = new ArrangementStateMachine(this.status);
	}

	public Long getDurationInMinutes(){
		if (this.plannedStartTime != null && this.plannedEndTime != null){
			return ChronoUnit.MINUTES.between(plannedStartTime, plannedEndTime);
		}else {
			return null;
		}
	}

	public void sendEvent(ArrangementEvents event){
		if(this.stateMachine.sendEvent(event)){
			this.status = this.stateMachine.getCurrentStatus();
			if (event.equals(ArrangementEvents.RUN) && this.status.equals(ExecutionStatus.FORMED)){
				this.startTime = LocalTime.now();
			}
			if (this.status.equals(ExecutionStatus.FINISHED) || this.status.equals(ExecutionStatus.ERROR)){
				this.endTime = LocalTime.now();
			}
		} else {
			throw new AS_15_8_PPT_Exception("Ошибка смены статуса мероприятия: " + stateMachine.getCurrentStatus() + " событием " + event);
		}
	}

}
