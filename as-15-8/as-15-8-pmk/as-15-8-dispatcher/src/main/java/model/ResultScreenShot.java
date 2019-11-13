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
@Table(schema = "results", name = "results")
@Data
public class ResultScreenShot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "arrangement_id", nullable = false)
    private Long arrangementId;

    @Lob
    @Column(name="screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] screenshot;

    @Lob
    @Column(name="etalon_screenshot", columnDefinition="bytea")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] etalonScreenshot;

    @Enumerated(EnumType.STRING)
    @Column(name="result", nullable=false)
    private CheckUnitJobResult result;
}
