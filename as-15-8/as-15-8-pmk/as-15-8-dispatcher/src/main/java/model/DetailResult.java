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
    @Column(insertable = false, updatable = false)
    private Long id;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private Result result;
}
