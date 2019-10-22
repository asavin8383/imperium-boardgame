package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Entity
@Table(schema="sor", name="addon_version")
@Data
public class AddonVersion implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="addon_version_generator")
	@SequenceGenerator(name="addon_version_generator", sequenceName="sor.addon_version_id_seq", allocationSize=1)
	@Column(name="id", nullable=false, updatable=false)
	private Long id;

    @Column(nullable = true, name = "reg_updatetime")
    private Date regUpdateTime;

    @Column(nullable = true, name = "delta_updatetime")
    private Date deltaUpdateTime;

    @Column(nullable=false, name = "ppn_dt")
	private Date ppnDate;

	@OneToMany(mappedBy = "addonVersion")
	@JsonIgnore
	private List<Addon> addons;

}
