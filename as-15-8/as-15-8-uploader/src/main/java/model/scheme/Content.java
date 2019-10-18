package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(schema="sor",name="content")
@Data
public class Content {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_generator")
    @SequenceGenerator(name="content_generator", sequenceName="sor.content_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @Column(nullable=false, name = "erdi_id")
    private String erdiId;

    @ManyToOne
    @JoinColumn(name = "init_content_version_id", referencedColumnName = "id")
    private ContentVersion contentVersion;

    @OneToMany(mappedBy = "content")
    @JsonIgnore
    private List<ContentHistory> contentHistory;

}
