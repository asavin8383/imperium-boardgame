package model.catalog;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.task.TaskJob;

/**
 * Запрещенный роскомнадзором ресурс
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="forbidden_resources")
@Data
public class ForbiddenResource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forbidden_resources_generator")
	@SequenceGenerator(name="forbidden_resources_generator", schema = "portal", sequenceName = "forbidden_resources_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@NotNull
	/**Наименование ресурса*/
	private String name;
	
	@NotNull
	/**URL ресурса*/
	private String url;
	
	/**Дата создания*/
	private Date creationDate;
	/**Дата последнего изменения*/
	private Date modificationDate;
	
	public ForbiddenResource() {
		this.creationDate = new Date();
	}
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(schema="portal", name="forbidden_resources_task_jobs",
				joinColumns=@JoinColumn(name="forbidden_resource_id"),
				inverseJoinColumns=@JoinColumn(name="task_job_id"))
	@JsonIgnore
	/**Список мероприятий, в которых проверяется данный запрещенный ресурс*/
	private List<TaskJob> taskJobs;
	
	@ManyToOne(optional=false)
	@JsonIgnore
	/**Организация, установившая запрет на ресурс*/
	private Organization organization;
	
}
