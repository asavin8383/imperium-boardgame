package model.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Роль пользователя в системе
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="user_roles")
@Data
public class UserRole implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
	private Long id;

	@Enumerated(EnumType.STRING)
	@NotNull
	private Role role;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(schema="portal", name="user_roles_users"
				,joinColumns=@JoinColumn(name="user_role_id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_role_id"))
				,inverseJoinColumns=@JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "FK_user_roles_users_user_id")))
	@JsonIgnore
	private List<User> users;
	
}
