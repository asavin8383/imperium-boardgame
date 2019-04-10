package model.catalog;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Государственнуе органы, устанавливающие запрет на ресурсы
 * @author asavin
 *
 */

@Entity
@Table(schema="portal", name="organizations")
public class Organization implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_generator")
	@SequenceGenerator(name="organizations_generator", schema = "portal", sequenceName = "organizations_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@NotNull
	/**Наименование организации*/
	private String name;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="organization")
	@JsonIgnore
	/**Список запрещенных данной организацией ресурсов*/
	private List<ForbiddenResource> forbiddenResourses; 

}
