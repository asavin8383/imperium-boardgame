package model.scheme;

import lombok.Data;
import javax.persistence.*;


@Entity
@Table(schema="sor",name="content")
@Data
public class Content {

    @Id
    private Long id;

    @Column(nullable=false, name = "erdi_id")
    private String erdiId;

    @ManyToOne
    @JoinColumn(name = "init_content_version_id", referencedColumnName = "id")
    private ContentVersion contentVersion;
}
