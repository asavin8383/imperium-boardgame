package model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.AccessToolUnit;
import lombok.Data;
import model.enums.AccessToolType;
import model.task.Arrangement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Средство доступа к запрещенным ресурсам (Поисковая система, VPN, анонимайзер и т.д.)
 * @author asavin
 *
 */

@Entity
@Table(schema="portal",name="access_tools")
@Data
public class AccessTool implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "access_tools_generator")
	@SequenceGenerator(name="access_tools_generator", schema = "portal", sequenceName = "access_tools_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;
	
	@NotNull
	@Column(unique = true)
	@Enumerated(EnumType.STRING)
	/**Наименование*/
	private AccessToolUnit name;

	@NotNull
	@Enumerated(EnumType.STRING)
	/**Тип средства доступа*/
	private AccessToolType type;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "accessTool")
	@JsonIgnore
	/**Список мероприятий, в которых проверяется данное средство доступа*/
	private List<Arrangement> arrangements;
}
