package model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Пользователи, работающие в системе
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="users")
@Data
@EqualsAndHashCode(exclude = "roles")
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
	
	@ManyToMany
	@JoinTable(schema="portal", name="user_roles_users"
			,joinColumns=@JoinColumn(name="user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_id"))
			,inverseJoinColumns=@JoinColumn(name="user_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_role_id")))
	@JsonIgnore
	private Set<UserRole> roles = new HashSet<>();
	
	@ManyToOne
	@JoinColumn(name="department_id", foreignKey = @ForeignKey(name = "FK_users_department_id"))
	@JsonIgnore
	private Department department;

	private String email;

	public User(){}

	public User(String userName){
		this.userName = userName;
	}

}
