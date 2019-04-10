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
 * ПАСД (программно-аппаратные средства доступа к запрещенным ресурсам)
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="vpn")
@Data
public class Vpn implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vpn_list_generator")
	@SequenceGenerator(name="vpn_list_generator", schema = "portal", sequenceName = "vpn_list_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@NotNull
	/**Наименование*/
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(schema="portal", name="vpn_task_jobs",
				joinColumns=@JoinColumn(name="vpn_id"),
				inverseJoinColumns=@JoinColumn(name="task_job_id"))
	@JsonIgnore
	/**Список мероприятий, в которых проверяется данный ПАСД*/
	private List<TaskJob> taskJobs;
}
