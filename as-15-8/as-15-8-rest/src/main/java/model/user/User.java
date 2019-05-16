package model.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
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
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
	private Long id;
	
	@Column(unique = true)
	@NotNull
	private String userName;
	private String firstName;
	private String secondName;
	
	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<UserRole> roles;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="department_id", foreignKey = @ForeignKey(name = "FK_users_department_id"))
	@JsonIgnore
	private Department department;

}
