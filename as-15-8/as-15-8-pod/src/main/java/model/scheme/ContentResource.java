package model.scheme;

import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.converters.ResourceTypeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(schema="sor", name="content_resources")
@Data
public class ContentResource implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_resources_generator")
	@SequenceGenerator(name="content_resources_generator", sequenceName="sor.content_resources_id_seq", allocationSize=1)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_id", referencedColumnName = "id")
	@JsonIgnore
	private Content content;

	@Column(nullable=false)
	private String value;

	@Column
	private Date ts;

	@Column(name = "resource_type_id")
	@Convert(converter = ResourceTypeConverter.class)
	private CheckUnitType checkUnitType;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_version_id", referencedColumnName = "id")
	@JsonIgnore
	private ContentVersion contentVersion;
}
