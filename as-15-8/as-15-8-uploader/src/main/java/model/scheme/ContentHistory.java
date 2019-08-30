package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;


@Entity
@Table(schema="sor",name="content_history")
@Data
public class ContentHistory {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_history_generator")
    @SequenceGenerator(name="content_historyn_generator", sequenceName="content_history_id_seq", allocationSize=1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private Content content;

    @Column(name = "ppn_dt")
    private Date ppnDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_version_id", referencedColumnName = "id")
    private ContentVersion contentVersion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "addon_version_id", referencedColumnName = "id")
    private AddonVersion addonVersion;

    @Column(nullable = false, name = "st_dt")
    private Date startDate;

    @Column(name = "end_dt")
    private Date endDate;
}
