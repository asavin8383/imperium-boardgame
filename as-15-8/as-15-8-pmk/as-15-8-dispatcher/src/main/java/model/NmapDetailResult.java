package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "results", name = "nmap_detail_results")
@Data
@EqualsAndHashCode(callSuper = true)
public class NmapDetailResult extends DetailResult{

    private String log;
}
