package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(schema = "results", name = "error_detail_results")
@Data
public class ErrorDetailResult {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JsonIgnore
    private Result result;

    private String error;
}
