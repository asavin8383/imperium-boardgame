package model.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Иерархическая модель отдела
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="departments")
@Data
public class Department implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="departments_generator")
	@SequenceGenerator(name="departments_generator", schema="portal", sequenceName="departments_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	@JsonIgnore
	private List<Department> children;
	
	@ManyToOne
	@JoinColumn(name="parent_id", referencedColumnName="id")
	private Department parent;
	
	@Column(nullable=false)
	@NotNull
	/** Наименование */
	private String name;
	
	@OneToMany(mappedBy="department", cascade=CascadeType.ALL)
	@JsonIgnore
	private List<User> users;

}
