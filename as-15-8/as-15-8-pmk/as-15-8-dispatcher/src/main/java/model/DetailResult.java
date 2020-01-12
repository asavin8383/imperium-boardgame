package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Data
public abstract class DetailResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long jobId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @Column(name = "job_id")
    @JsonIgnore
    private Result result;
}
