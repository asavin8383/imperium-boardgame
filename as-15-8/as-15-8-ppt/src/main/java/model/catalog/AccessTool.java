package model.catalog;

import enums.AccessToolUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import model.enums.AccessToolType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Средство доступа к запрещенным ресурсам (Поисковая система, VPN, анонимайзер и т.д.)
 * @author asavin
 *
 */

@Entity
@Table(schema="portal",name="access_tools",
	uniqueConstraints = @UniqueConstraint(name = "uq_access_tools_id_type", columnNames = {"id", "type"}))
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccessTool implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "access_tools_generator")
	@SequenceGenerator(name="access_tools_generator", schema = "portal", sequenceName = "access_tools_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;
	
	/**Наименование*/
	@NotNull
	@Enumerated(EnumType.STRING)
	@EqualsAndHashCode.Include
	private AccessToolUnit name;

	/**Тип средства доступа*/
	@NotNull
	@Enumerated(EnumType.STRING)
	private AccessToolType type;
	
	@OneToOne(mappedBy = "accessTool")
	@JoinColumns({
			@JoinColumn(name = "id", referencedColumnName = "access_tool_id"),
			@JoinColumn(name = "type", referencedColumnName = "access_tool_type")
	})
	private SearchSystemParameters searchSystemParameters;

	@OneToOne(mappedBy = "accessTool")
	@JoinColumns({
			@JoinColumn(name = "id", referencedColumnName = "access_tool_id"),
			@JoinColumn(name = "type", referencedColumnName = "access_tool_type")
	})
	private VPN_Parameters vpnParameters;

	@OneToOne(mappedBy = "accessTool")
	@JoinColumns({
			@JoinColumn(name = "id", referencedColumnName = "access_tool_id"),
			@JoinColumn(name = "type", referencedColumnName = "access_tool_type")
	})
	private ProxyParameters proxyParameters;

	@OneToOne(mappedBy = "accessTool")
	@JoinColumns({
			@JoinColumn(name = "id", referencedColumnName = "access_tool_id"),
			@JoinColumn(name = "type", referencedColumnName = "access_tool_type")
	})
	private AnonymizerParameters anonymizerParameters;
}
