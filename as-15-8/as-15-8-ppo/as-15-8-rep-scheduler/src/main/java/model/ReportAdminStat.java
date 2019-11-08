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
@Table(schema = "dm", name = "v_api_reg_reports_admin_stat")
public class ReportAdminStat
{
    @Id long rowid;

    @Column long rep_tp_id;

    @Column
    String  label;

    @Column
    String status;

    @Column
    long cnt;

}
