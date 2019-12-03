package model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    long rep_id;

    @Column
    String msr_prd_tp;

    @Column
    String msr_prd_caption;

    @Column
    int rep_tp_id;

    @Column
    String format;

    @Column
    long ppn_dttm;

}
