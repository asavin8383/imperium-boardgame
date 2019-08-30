package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(schema="sor", name="content_del")
@Data
public class ContentDel implements Serializable {

	@Id
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_id", referencedColumnName = "id")
	private Content content;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_version_id", referencedColumnName = "id")
	private ContentVersion contentVersion;
}
