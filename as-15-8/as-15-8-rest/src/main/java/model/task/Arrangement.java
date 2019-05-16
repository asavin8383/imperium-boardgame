package model.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.catalog.AccessTool;
import model.enums.ExecutionStatus;

/**Мероприятие в рамках формализованного задания*/

@Entity
@Table(schema="portal", name="arrangements")
@Data
public class Arrangement implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
	private Long id;

	@NotNull
	/**Название мероприятия*/
	private String title;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	/**Статус мероприятия*/
	private ExecutionStatus status;
	
	/**Дата создания*/
	private Date creationDate;
	/**Дата начала*/
	private Date startDate;
	/**Дата окончания*/
	private Date endDate;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="formal_task_id", foreignKey = @ForeignKey(name = "FK_arrangements_formal_task_id"))
	@JsonIgnore
	/**Формализованное задание на проведение мероприятий*/
	private FormalTask formalTask;
	
	@ManyToOne(optional = false)
	@JoinColumn(name="access_tool_id", foreignKey = @ForeignKey(name = "FK_arrangements_access_tool_id"))
	@JsonIgnore
	/**Список поисковых систем для проверки*/
	private AccessTool accessTool;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "arrangement")
	private List<ArrangementItem> arrangementItems;
	
	/**Результат проведения мероприятия*/
    private String result;
	
	public Arrangement() {
		this.creationDate = new Date();
		this.status = ExecutionStatus.PLANNED;
	}
}
