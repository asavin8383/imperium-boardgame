package model.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.catalog.SearchSystem;
import model.catalog.Vpn;

/**Мероприятие в рамках формализованного задания*/

@Entity
@Table(schema="portal", name="task_jobs")
@Data
public class TaskJob implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="task_jobs_generator")
	@SequenceGenerator(name="task_jobs_generator", schema="portal", sequenceName="tas_jobs_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@NotNull
	/**Название мероприятия*/
	private String title;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	/**Статус мероприятия*/
	private TaskOrJobStatus status;
	
	/**Дата создания*/
	private Date creationDate;
	/**Дата начала*/
	private Date startDate;
	/**Дата окончания*/
	private Date endDate;
	
	@ManyToOne(optional=false)
	@JsonIgnore
	/**Формализованное задание на проведение мероприятий*/
	private FormalTask formalTask;
	
	@ManyToMany(mappedBy="taskJobs")
	@JsonIgnore
	/**Список поисковых систем для проверки*/
	private List<SearchSystem> searchSystems;
	
	@ManyToMany(mappedBy="taskJobs")
	@JsonIgnore
	/**Список ПАСД для проверки*/
	private List<Vpn> vpnList;

	@OneToMany(cascade=CascadeType.ALL, mappedBy = "taskJob")
	private List<JobItem> jobItems;
	
	/**Результат проведения мероприятия*/
    private String result;
	
	public TaskJob() {
		this.creationDate = new Date();
		this.status = TaskOrJobStatus.PLANNED;
	}
}
