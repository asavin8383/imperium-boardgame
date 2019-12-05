package model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
@Data
@Entity
@Table(schema = "dm", name = "rep_tp")
public class ReportType
{
    @Id
    private long rep_tp_id;

    @Column
    private String name;

    @Column
    private String type;

    @Column
    private String af_id;

    @Column
    private String periods_label;

    @Column
    private String shrt_nm;

    @Column
    private String rptdesign;

}
