package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(schema="sor", name="content_del")
@Data
public class ContentDel implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_del_generator")
	@SequenceGenerator(name="content_del_generator", sequenceName="sor.content_del_id_seq", allocationSize=1)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_id", referencedColumnName = "id")
	private Content content;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_version_id", referencedColumnName = "id")
	private ContentVersion contentVersion;
}
