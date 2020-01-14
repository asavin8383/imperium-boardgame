package model;

import lombok.Data;
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
    private Long jobId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
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
