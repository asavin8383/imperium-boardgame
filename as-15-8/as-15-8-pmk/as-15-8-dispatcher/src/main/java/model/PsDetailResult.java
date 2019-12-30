package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "results", name = "ps_detail_results")
@Data
@EqualsAndHashCode(callSuper = true)
public class PsDetailResult extends DetailResult{

    private String description;
}
