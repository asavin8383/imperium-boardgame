package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(schema="sor", name="register")
@Data
public class Register implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="register_generator")
	@SequenceGenerator(name="register_generator", sequenceName="sor.register_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

	@Column(nullable=false, name = "updatetime")
	private Date updateTime;

	@Column(name="updatetimeurgently")
	private Date updateTimeUrgently;

	@Column(name="formatversion", nullable=false)
	private String formatVersion;
}
