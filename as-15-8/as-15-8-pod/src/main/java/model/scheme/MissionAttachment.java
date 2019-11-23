package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by san
 * Date: 23.11.2019
 */
@Entity
@Table(schema = "sor", name = "mission_attachments")
@Data
public class MissionAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    private Mission mission;

    @Lob
    @Column(name="screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    @JsonIgnore
    private byte[] attachment;

}
