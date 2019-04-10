package model.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Пользователи, работающие в системе
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="users")
@Data
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_generator")
	@SequenceGenerator(name="users_generator", schema = "portal", sequenceName = "users_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@NotNull
	private String fullName;
	private String name;
	private String surname;
	private String patronymic;
	
	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<UserRole> roles;
	
	@ManyToOne(optional=false)
	@NotNull
	@JsonIgnore
	private Department department;

}
