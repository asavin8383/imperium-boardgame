package model.scheme;

import lombok.Data;

import javax.persistence.*;


@Entity
@Table(schema="sor",name="addon")
@Data
public class Addon {
    @Id
    private Long id;

    private String data;

    @ManyToOne(optional = false)
    @JoinColumn(name = "addon_version_id", referencedColumnName = "id")
    private AddonVersion addonVersion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private Content content;
}
