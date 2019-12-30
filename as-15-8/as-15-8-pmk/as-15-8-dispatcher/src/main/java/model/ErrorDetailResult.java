package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "results", name = "error_detail_results")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorDetailResult extends DetailResult{

    private String error;
}
