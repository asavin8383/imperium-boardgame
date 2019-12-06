package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * User: asinjavin
 * Date: 01.11.2019
 * Time: 12:48
 */
@Data
@Entity
@Table(schema = "dm", name = "param_reports")
public class ParamReport
{
    @Id
    long repId;

    @Column
    String msr_prd_tp;

    @Column
    String msr_prd_caption;

    @Column
    int repTpId;

    @Column
    String format;

    @Column
    String mime;

    @Column
    Timestamp ppn_dttm;

    @ToString.Exclude
    @Column
    @JsonView(Views.Data.class)
    private byte[] data;

}
