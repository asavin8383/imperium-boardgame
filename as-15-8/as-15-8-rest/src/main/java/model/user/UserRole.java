package model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.enums.Role;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="user_roles_generator")
	@SequenceGenerator(name="user_roles_generator", schema="portal", sequenceName="user_roles_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@NotNull
    @Column(unique = true)
	private Role role;
	
	@ManyToMany(mappedBy="roles", fetch = FetchType.EAGER)
	@JsonIgnore
	private List<User> users = new ArrayList<>();
	
}
