package model.catalog;

import java.io.Serializable;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import model.task.TaskJob;

/**
 * Поисковая система
 * @author asavin
 *
 */

@Entity
@Table(schema="portal",name="search_systems")
@Data
public class SearchSystem implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_systems_generator")
	@SequenceGenerator(name="search_systems_generator", schema = "portal", sequenceName = "search_systems_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@NotNull
	/**Наименование*/
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(schema="portal", name="search_systems_task_jobs",
				joinColumns=@JoinColumn(name="search_system_id"),
				inverseJoinColumns=@JoinColumn(name="task_job_id"))
	@JsonIgnore
	/**Список мероприятий, в которых проверяется данная поисковая система*/
	private List<TaskJob> taskJobs;
}
