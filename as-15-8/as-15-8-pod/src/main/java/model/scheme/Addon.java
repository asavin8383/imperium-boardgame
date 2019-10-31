package model.scheme;

import lombok.Data;

import javax.persistence.*;


@Entity
@Table(schema="sor",name="addon")
@Data
public class Addon {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "addon_generator")
    @SequenceGenerator(name = "addon_generator", sequenceName = "sor.addon_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "addon_version_id", referencedColumnName = "id")
    private AddonVersion addonVersion;

    @Column
    private Long content_id;

    @Column
    private Long orig_id;

    @Column
    private String infoTypeId;

    @Column
    private Long visitorsCntRussia;

    @Column
    private Long visitorsCntWorld;
}
