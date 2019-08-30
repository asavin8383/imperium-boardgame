package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(schema="sor", name="decision")
@Data
public class Decision implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="decision_generator")
	@SequenceGenerator(name="decision_generator", sequenceName="decision_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@Column(nullable=false)
	private Date date;

	@Column(nullable=false)
	private String number;

	@Column(nullable=false)
	private String org;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_id", referencedColumnName = "id")
	private Content content;

	@ManyToOne(optional = false)
	@JoinColumn(name = "content_version_id", referencedColumnName = "id")
	private ContentVersion contentVersion;
}
