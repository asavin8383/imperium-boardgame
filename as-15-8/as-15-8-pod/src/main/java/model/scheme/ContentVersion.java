package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(schema="sor", name="content_version")
@Data
public class ContentVersion implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_version_generator")
	@SequenceGenerator(name="content_version_generator", sequenceName="sor.content_version_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@Column(nullable=true, name = "reg_updatetime")
	private Date regUpdateTime;

	@Column(nullable=true, name = "delta_updatetime")
	private Date deltaUpdateTime;

	@Column(nullable=false, name = "ppn_dt")
	private Date ppnDate;

	@Column(nullable=true, name = "delta_id")
	private Long deltaId;

}
