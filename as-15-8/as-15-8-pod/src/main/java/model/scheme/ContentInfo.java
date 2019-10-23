package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.converters.BlockTypeConverter;
import model.converters.UrgencyTypeConverter;
import model.enums.BlockType;
import model.enums.UrgencyType;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(schema="sor",name="content_info")
@Data
public class ContentInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="content_info_generator")
    @SequenceGenerator(name="content_info_generator", sequenceName="sor.content_info_id_seq", allocationSize=1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    @JsonIgnore
    private Content content;

    @Column(nullable=false, name = "includetime")
    private Date includeTime;

    @Column
    private String hash;

    @Column(nullable=false)
    private Date ts;

    @Column(name = "entrytype_id", nullable = false)
    @JsonIgnore
    private String entryTypeId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrytype_id", referencedColumnName = "id", insertable = false, updatable = false)
    private EntryType entryType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_version_id", referencedColumnName = "id")
    @JsonIgnore
    private ContentVersion contentVersion;

    @Column(name = "blocktype")
    @Convert(converter = BlockTypeConverter.class)
    private BlockType blockType;


    @Column(name = "urgencytype_id")
    @Convert(converter = UrgencyTypeConverter.class)
    private UrgencyType urgencyType;





}
