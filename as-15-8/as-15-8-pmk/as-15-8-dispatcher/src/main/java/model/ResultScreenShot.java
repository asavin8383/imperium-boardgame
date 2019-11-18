package model;

import enums.CheckUnitJobResult;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by san
 * Date: 10.11.2019
 */

@Entity
@Table(schema = "results", name = "result_screenshots")
@Data
public class ResultScreenShot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne(optional = false)
    @MapsId
    private Result result;

    @Lob
    @Column(name="screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] screenshot;

    @Lob
    @Column(name="etalon_screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] etalonScreenshot;

}
