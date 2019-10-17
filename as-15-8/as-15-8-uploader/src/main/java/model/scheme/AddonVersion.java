package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(schema="sor", name="addon_version")
@Data
public class AddonVersion implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="addon_version_generator")
	@SequenceGenerator(name="addon_version_generator", sequenceName="sor.addon_version_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@Column(nullable=false, name = "ppn_dt")
	private Date ppnDate;

	@Column(nullable=false)
	private Integer success = 1;
}
