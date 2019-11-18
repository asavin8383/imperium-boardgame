package model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import enums.ArrangementEvents;
import enums.ExecutionStatus;
import exceptions.AS_15_8_PPT_Exception;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import model.Views;
import model.traffic.Traffic;
import stateMachine.ArrangementStateMachine;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

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
	@Getter
	private ExecutionStatus status;
	
	/**Дата создания*/
	@ToString.Include
	@JsonView(Views.Brief.class)
	private LocalDateTime creationDate;

	/**Плановая время начала*/
	@JsonView(Views.Brief.class)
	private LocalTime plannedStartTime;

	/**Плановое время окончания*/
	@JsonView(Views.Brief.class)
	private LocalTime plannedEndTime;

	/**Дата начала*/
	@JsonView(Views.Brief.class)
	private LocalDate startDate;

	/** Дата завершения */
	@JsonView(Views.Brief.class)
	private LocalDate completionDate;

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

	@ManyToOne
	@JoinColumn(name = "traffic_id")
	private Traffic traffic;

	/**Текстовый комментарий*/
	@JsonView(Views.Brief.class)
    private String comment;

	/**Дополнительная текстовая информация*/
	@JsonView(Views.Brief.class)
	private String info;

    @Transient
    private ArrangementStateMachine stateMachine;
	
	public Arrangement() {
		this.creationDate = LocalDateTime.now();
		this.status = ExecutionStatus.NEW;
	}

	public Long getDurationInMinutes(){
		if (this.plannedStartTime != null && this.plannedEndTime != null){
			return ChronoUnit.MINUTES.between(plannedStartTime, plannedEndTime);
		}else {
			return null;
		}
	}

	public void sendEvent(ArrangementEvents event, LocalDate eventDate){
		this.stateMachine = new ArrangementStateMachine(this.status);
		if(this.stateMachine.sendEvent(event)){
			this.status = this.stateMachine.getCurrentStatus();
			if (event.equals(ArrangementEvents.RUN) && this.status.equals(ExecutionStatus.RUNNING)){
				this.startDate = eventDate;
			}
			if (this.status.equals(ExecutionStatus.FINISHED)){
				this.completionDate = eventDate;
			}
		} else {
			throw new AS_15_8_PPT_Exception("Ошибка смены статуса мероприятия: " + stateMachine.getCurrentStatus() + " событием " + event);
		}
	}

	public Long getFormalTaskId(){
		return this.formalTask == null ? null : this.formalTask.getId();
	}

}
