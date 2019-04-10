package model.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_roles_generator")
	@SequenceGenerator(name="user_roles_generator", schema = "portal", sequenceName = "user_roles_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@NotNull
	private Role role;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(schema="portal", name="user_roles_users"
				,joinColumns=@JoinColumn(name="user_role_id")
				,inverseJoinColumns=@JoinColumn(name="user_id"))
	@JsonIgnore
	private List<User> users;
	
}
