package model.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Неформализованное задание на проведение проверок
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="informal_tasks")
@Data
public class InformalTask implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="informal_tasks_generator")
	@SequenceGenerator(name="informal_tasks_generator", schema="portal", sequenceName="informal_tasks_id_seq", allocationSize=1)
	@Column(name="id", updatable=false, nullable=false)
	private Long id;
	
	/**Название*/
	private String title;
	/**Автор задания (сотрудник РКН)*/
	private String author;
	/**Согласовано или нет*/
	private boolean confirmed;
	/**Дата создания*/
	private Date creationDate;
	/**Приложение к заданию(файлы, документы и т.п.)*/
	private byte[] content;
	
	@OneToMany(mappedBy="informalTask", cascade=CascadeType.ALL)
	@JsonIgnore
	/**Список формализованных заданий*/
	private List<FormalTask> formalTasks;
	
	public InformalTask() {
		this.creationDate = new Date();
	}
}
