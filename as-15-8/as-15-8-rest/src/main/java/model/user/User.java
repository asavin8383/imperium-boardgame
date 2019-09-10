package model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Пользователи, работающие в системе
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="users")
@Data
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="users_generator")
	@SequenceGenerator(name="users_generator", schema="portal", sequenceName="users_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	@JsonView(Views.Id.class)
	private Long id;
	
	@Column(unique = true)
	@NotNull
	@JsonView(Views.Brief.class)
	private String userName;
	@JsonView(Views.Brief.class)
	private String firstName;
	@JsonView(Views.Brief.class)
	private String secondName;
	
	@ManyToMany
	@JoinTable(schema="portal", name="user_roles_users"
			,joinColumns=@JoinColumn(name="user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_id"))
			,inverseJoinColumns=@JoinColumn(name="user_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_role_id")))
	@JsonIgnore
	private List<UserRole> roles = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name="department_id", foreignKey = @ForeignKey(name = "FK_users_department_id"))
	@JsonIgnore
	private Department department;

	@JsonView(Views.Brief.class)
	private String email;

	public User(){}

	public User(String userName){
		this.userName = userName;
	}

}
