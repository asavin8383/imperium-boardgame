package model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(schema = "results", name = "nmap_detail_results")
@Data
public class NmapDetailResult {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private Result result;

    private String log;
}
